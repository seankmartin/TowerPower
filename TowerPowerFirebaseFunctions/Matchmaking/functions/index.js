const functions = require('firebase-functions');
const admin = require('firebase-admin');
const afar = require('afar')

admin.initializeApp(functions.config().firebase);
const db = admin.firestore()
const firestore_collection = '/matchmaking'
const firestore_location = firestore_collection + '/{userID}'

var radius = 0.5; //In kilometres

exports.matchmaking = 
functions.firestore.document(firestore_location).onCreate(
    (event) => {
        var user_list = []
        return allUsers = db.collection(firestore_collection).get()
        .then(snapshot => {
            //Retrieve all the users
            snapshot.forEach(doc => {
                user_list.push(doc.data())
            });
            console.log("users are", user_list)

            if(user_list.length < 3) {
                console.log("not enough users to build a team at the moment");
                return true
            }
        
            var matched_users = []

            for(i = 0; i < user_list.length - 2; ++i) {
                var user_matches = [user_list[i]]
                for(j = i + 1; j < user_list.length; ++j) {
                    var location1 = user_list[i].location;
                    var location2 = user_list[j].location;
                    var distance = afar(location1.latitude, location1.longitude,
                                        location2.latitude, location2.longitude);
                    if(distance < 2 * radius) {
                            user_matches.push(user_list[j])
                    }
                }
                var different_roles = true
                const random_role_name = "random"
                for(k = 0; k < user_matches.length - 1; k++) {
                    for(j = k + 1; j < user_matches.length; j++) {
                        if(user_matches[k].role !== random_role_name &&
                            user_matches[k].role === user_matches[j].role) {
                            different_roles = false;
                        }
                    }
                }
                if((user_matches.length >= 3) && different_roles) {
                    matched_users = user_matches
                    break;
                }
            }

            console.log("matched users are", matched_users)

            if(matched_users.length === 0) {
                console.log("No suitable team found")
                return true
            }
            
            //Create a batch to create a team, remove that team from the matchmaking
            //And then update the user database entry to contain their new team
            var batch = db.batch()
    
            // Create two references to documents with auto-generated ID
            var new_team_ref = db.collection('teams').doc();
            var new_game_ref = db.collection('teams').doc(new_team_ref.id).collection('games').doc();
            var team_id = new_team_ref.id
            var team = {current_game: new_game_ref.id}
            var centre_x = 0
            var centre_y = 0

            //Delete these three found users from the database
            for(i = 0; i < 3; ++i) {
                var user_match_ref = db.collection("matchmaking").doc(matched_users[i].userID)
                var user_ref = db.collection("users").doc(matched_users[i].userID)
                batch.delete(user_match_ref)
                batch.set(user_ref, {"team_id": team_id}, {merge: true})
                matched_users[i]["response"] = "pending"
                team["user" + i.toString()] = matched_users[i]
                centre_x += matched_users[i].location.latitude
                centre_y += matched_users[i].location.longitude
            }
            batch.set(new_team_ref, team);

            //Get the centroid of the triangle of user locations
            centre_x /= 3.0
            centre_y /= 3.0
            var game = {latitude: centre_x, longitude: centre_y}
            var starting_inventory = {hints: 0, materials: 0}
            game["inventory"] = starting_inventory
            game["time_bonus"] = 0
            var current_time = admin.firestore.FieldValue.serverTimestamp()
            game["start_time"] = current_time
            batch.set(new_game_ref, game)
            return batch.commit()
        })
        .catch(err => {
            console.log('Error getting documents', err);
            return true
        });
    }
)