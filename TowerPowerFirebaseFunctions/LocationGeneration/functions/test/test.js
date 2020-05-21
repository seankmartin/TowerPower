var assert = require('assert');
const chai = require('chai')
const placeLocations = require('../index_UT');

sample_locations = [ { latitude: 53.3498053, longitude: -6.2603097 },
  { latitude: 53.340857, longitude: -6.2556567 },
  { latitude: 53.338535, longitude: -6.252916000000002 },
  { latitude: 53.341494, longitude: -6.250813 },
  { latitude: 53.33894379999999, longitude: -6.256086900000001 },
  { latitude: 53.344883, longitude: -6.252473999999999 },
  { latitude: 53.3437935, longitude: -6.2545716 },
  { latitude: 53.3416667, longitude: -6.250027499999999 },
  { latitude: 53.3420277, longitude: -6.2547945 },
  { latitude: 53.3412198, longitude: -6.255339200000001 },
  { latitude: 53.3406652, longitude: -6.257646699999999 },
  { latitude: 53.3439329, longitude: -6.2567401 },
  { latitude: 53.34059009999999, longitude: -6.253981599999999 },
  { latitude: 53.342039, longitude: -6.256721 },
  { latitude: 53.3402225, longitude: -6.2549209 },
  { latitude: 53.3402225, longitude: -6.2549209 },
  { latitude: 53.3421561, longitude: -6.255217 },
  { latitude: 53.339429, longitude: -6.251456999999998 },
  { latitude: 53.341853, longitude: -6.256037 },
  { latitude: 53.3442039, longitude: -6.250494999999999 },
  { latitude: 53.34197750000001, longitude: -6.2587796 },
  { latitude: 53.34098289999999, longitude: -6.256605899999998 },
  { latitude: 53.339167, longitude: -6.2522353 },
  { latitude: 53.34054640000001, longitude: -6.257952299999999 },
  { latitude: 53.342211, longitude: -6.256608999999999 },
  { latitude: 53.3424652, longitude: -6.257680799999998 },
  { latitude: 53.3409059, longitude: -6.252502499999999 },
  { latitude: 53.342171, longitude: -6.258186 },
  { latitude: 53.34056, longitude: -6.248605200000001 },
  { latitude: 53.34189139999999, longitude: -6.255965499999999 },
  { latitude: 53.3412109, longitude: -6.2485727 },
  { latitude: 53.34109840000001, longitude: -6.2544821 },
  { latitude: 53.33988480000001, longitude: -6.2533478 },
  { latitude: 53.3383367, longitude: -6.253105 },
  { latitude: 53.3418678, longitude: -6.250558300000001 },
  { latitude: 53.3416381, longitude: -6.258289899999999 },
  { latitude: 53.3423388, longitude: -6.2501342 },
  { latitude: 53.3409467, longitude: -6.2581414 },
  { latitude: 53.33942769999999, longitude: -6.2563471 },
  { latitude: 53.3422034, longitude: -6.256355000000002 },
  { latitude: 53.3428112, longitude: -6.249820499999998 },
  { latitude: 53.342172, longitude: -6.254967700000001 },
  { latitude: 53.340616, longitude: -6.257343899999999 },
  { latitude: 53.341026, longitude: -6.256978000000001 },
  { latitude: 53.3396584, longitude: -6.249164599999999 },
  { latitude: 53.34208749999999, longitude: -6.257756 },
  { latitude: 53.340551, longitude: -6.253949 },
  { latitude: 53.3423281, longitude: -6.251963900000001 },
  { latitude: 53.3426293, longitude: -6.257277600000001 },
  { latitude: 53.3427115, longitude: -6.257427 },
  { latitude: 53.3387739, longitude: -6.2526225 },
  { latitude: 53.33906, longitude: -6.250420999999999 },
  { latitude: 53.3409068, longitude: -6.2496087 },
  { latitude: 53.3440569, longitude: -6.2499547 },
  { latitude: 53.33917779999999, longitude: -6.256760399999999 },
  { latitude: 53.33820830000001, longitude: -6.254229200000001 },
  { latitude: 53.3432805, longitude: -6.257701299999999 },
  { latitude: 53.3417972, longitude: -6.2521826 },
  { latitude: 53.3439941, longitude: -6.2532784 },
  { latitude: 53.342039, longitude: -6.258268 } ]


describe('Return Promise', function() {
  describe('#indexOf()', function() {
    it('should return -1 when the value is not present', function() {
      assert.equal([1,2,3].indexOf(4), -1);
    });
  });
});

describe('Test Remove Element', function(){
  
  var input = [
    {
      'latitude': 5,
      'longitude': 6
    },
    {
      'latitude': 15,
      'longitude': 26
    } 
];
  var output = [
    {
      'latitude': 15,
      'longitude': 26
    } 
];

  placeLocations.RemoveElement(input, {
    'latitude': 5,
    'longitude': 6
  });

  it('Should Remove an Element', function(){
    assert.equal(input[0].latitude, output[0].latitude);
  });
});


describe('Test Farthest Location', function(){
  
  var input = [
    {
      'latitude': 53.44564324,
      'longitude':  -5.3435335
    },
    {
      'latitude': 15.123123,
      'longitude': 26.213123
    } 
  ];

  var centre = {
    'latitude' : 53.44566,
    'longitude': -6.3435335
  };

  var return_val = placeLocations.FindNFarthestLocation(input, centre);

  it('Should Return one of', function(){
    chai.assert.oneOf(return_val.latitude, [input[0].latitude, input[1].latitude]);
  });
});

describe('Test fetching three towers', function(){

  var centre = {
    'latitude' : 53.44566,
    'longitude': -6.3435335
  };

  var return_val = placeLocations.FetchTowers(sample_locations, centre);
  console.log(return_val);
  it('Length should be equal to', function(){
    assert.equal(Object.keys(return_val).length, 3);
  });
});

describe('Test fetching three bases', function(){

  var centre = {
    'latitude' : 53.44566,
    'longitude': -6.3435335
  };

  var return_val = placeLocations.FetchBases(sample_locations, centre);
  it('Length should be equal to', function(){
    assert.equal(Object.keys(return_val).length, 3);
  });
});

describe('Test fetching thirteen hints', function(){

  var centre = {
    'latitude' : 53.44566,
    'longitude': -6.3435335
  };

  var return_val = placeLocations.FetchHints(sample_locations, centre);
  it('Length should be equal to', function(){
    assert.equal(Object.keys(return_val).length, 13);
  });
}); 

describe('Test fetching twenty one locations', function(){

  var centre = {
    'latitude' : 53.44566,
    'longitude': -6.3435335
  };

  var return_val = placeLocations.FetchMaterials(sample_locations, centre);
  it('Length should be equal to', function(){
    assert.equal(Object.keys(return_val).length, 21);
  });
}); 

describe('Test fetching remaining  locations', function(){

  var centre = {
    'latitude' : 53.44566,
    'longitude': -6.3435335
  };

  var return_val = placeLocations.OtherLocations(sample_locations, centre);
  it('Length should be equal to', function(){
    assert.equal(Object.keys(return_val).length, 10);
  });
}); 