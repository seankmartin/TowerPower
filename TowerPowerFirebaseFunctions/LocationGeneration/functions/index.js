const functions = require('firebase-functions');
const randomLocation = require('random-location')
const orderLocation = require('sort-by-distance')
var randomise = require('randomise-array')

//NOTE the arrow operator => creates arrow functions, very similar to lambda
//functions in other languages
firestore_location = '/teams/{teamID}/games/{gameID}'

var googleMapsClient = require('@google/maps').createClient({
    key: "GET_YOUR_OWN",
    Promise: Promise
});

// Method that uses nested promises to make back to back calls to placesNearby
// NOTE: eslint warns not to use nested promises
// NOTE: Timeout of 5000 milliseconds between placesNearby calls required by the API
function allPlacesNearby(query, soFar = []) {
    console.info(`sending placesNearby query: ${JSON.stringify(query)}`);
    return new Promise((resolve, reject) => googleMapsClient.placesNearby(query, (err, response) => (err ? reject(err) : resolve(response))))
    .then(result => {
      console.info(`received ${result.json.results.length} results`);
      const allResults = soFar.concat(result.json.results);
      const { next_page_token: nextPageToken } = result.json;
      if (nextPageToken) {
        return new Promise(resolve => setTimeout(resolve, 5000)).then(() => allPlacesNearby({
          "location": query.location,
          "pagetoken": nextPageToken
        }, allResults));
      }
      return allResults;
    });
  }

function removeElement(array, element) {
    var index = -1;
    for (i=0; i < array.length; i++)
    {
        if(element.latitude === array[i].latitude &&
             element.longitude === array[i].longitude){
                 index = i;
                 break;
             }
    }

    if (index !== -1) {
        array.splice(index, 1);
    }
    else{
        console.log("Element Not Found");
    }
}

function findNFarthestLocation(locations, centre, N=1){

    var farthest_location = {};
    num_locations = locations.length;

    const input_paramter = {
        yName: 'latitude',
        xName: 'longitude'
    }
    
    var ordered_locations = orderLocation(
        centre,
        locations,
        input_paramter
    );

    farthest_location['latitude'] = ordered_locations[num_locations-N].latitude;
    farthest_location['longitude'] = ordered_locations[num_locations-N].longitude;

    return farthest_location;
}

function fetchTowers(locations, centre){

    var tower_locations = {};

    /*
     Removes the last location since it is usually out of the specified
     radius
    */ 
    var remove_farthest_location = findNFarthestLocation(locations, centre);
    removeElement(locations, remove_farthest_location);
    console.log("INFO: The farthest location is removed.");
    console.log("59 Locations Added");
    tower_locations['tower1'] = findNFarthestLocation(locations, centre);
    removeElement(locations, tower_locations['tower1']);
    tower_locations['tower2'] = findNFarthestLocation(locations, centre, 45);
    removeElement(locations, tower_locations['tower2']);
    tower_locations['tower3'] = findNFarthestLocation(locations, centre, 12);
    removeElement(locations, tower_locations['tower3']);

    return tower_locations;

}

function fetchRandomLocation(locations){

    var random_index = Math.floor(Math.random() * locations.length);
    var return_location = locations[random_index];
    removeElement(locations, return_location);

    return return_location;
}

function fetchBases(locations){
    var base_locations = {};

    base_locations['base1'] = fetchRandomLocation(locations);
    base_locations['base2'] = fetchRandomLocation(locations);
    base_locations['base3'] = fetchRandomLocation(locations);

    return base_locations;
}

function fetchHints(locations){
    var hint_locations = {};
    
    var i=0;
    for(i=0; i<13; i++){
        hint_locations['hint' + (i + 1)] = fetchRandomLocation(locations);
    }

    return hint_locations;
}

function fetchMaterials(locations){
    var material_locations = {};
    
    var i=0;
    for(i=0; i<21; i++){
        material_locations['material' + (i + 1)] = fetchRandomLocation(locations);
    }

    return material_locations;
}

function otherLocations(locations){
    var other_locations = {};

    var i=0;
    for(i=0; i<locations.length; i++){
        other_locations['location' + (i + 1)] = fetchRandomLocation(locations);
    }

    return other_locations;
}


//Note, perhaps this should be on on write
exports.PlaceLocations = 
functions.firestore.document(firestore_location).onCreate(
    (event) => {
        
        var document_data = event.data.data()

        //This will be set when we call this function from firebase
        var centre = {
            latitude: document_data.latitude,
            longitude: document_data.longitude
        }

        //TODO decide if we should take in a radius
        var search_radius = 400
        // Making three calls to placesNearby to get 60 non-duplicate places
        var num_points_required = 60
        
        //TODO it could be a good idea to put a timeout in this
        //Not sure what the unit for this timeout is though
        var places_request = {
            language: 'en',
            location: centre,
            radius: search_radius,
            rankby: 'prominence',
        }

        return allPlacesNearby(places_request)
        .then(response =>{

            console.log("Found ", response.length, " places from API, generating the other",
             num_points_required - response.length, " points randomly")

            var list_locations = []
            for(i = 0 ; i < response.length; i++)
            {
                var location = response[i].geometry.location;
                var push_location = {}
                push_location["latitude"] = location.lat
                push_location["longitude"] = location.lng
                list_locations.push(push_location)
            }

            //If not enough points, return some random ones
            for(i = 0; i < num_points_required - response.length; ++i) {
                var randomPoint = 
                randomLocation.randomCirclePoint(centre, search_radius)
                list_locations.push(randomPoint)
            }

            var towers = {};
            var materials = {};
            var bases = {};
            var hints = {};
            var others = {};

            console.log("Total Locations Retrieved: " + list_locations.length);
            console.log(list_locations);
            // 3 Towers
            towers = fetchTowers(list_locations, centre);
            // 3 Base
            bases = fetchBases(list_locations);
            // 13 hints
            hints = fetchHints(list_locations);
            // 21 Material
            materials = fetchMaterials(list_locations);
            // Rest
            others = otherLocations(list_locations);

            console.log(towers);
            console.log(bases);
            console.log(hints);
            console.log(materials);
            console.log(others);

            event.data.ref.set({towers}, {merge: true});
            event.data.ref.set({materials}, {merge: true});
            event.data.ref.set({bases}, {merge: true});
            event.data.ref.set({hints}, {merge: true});
            return event.data.ref.set({others}, {merge: true});
        })
        .catch(err => {
        console.error(`err: ${err}`)
    })
    }
)