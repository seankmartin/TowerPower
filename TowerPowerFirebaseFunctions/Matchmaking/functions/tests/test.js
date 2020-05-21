const expect = require('chai').expect
const Matchmaking = require('../local_match.js').Matchmaking
const afar = require('afar')
var radius = 0.5; //In kilometres

//Create a user
function User(id, role, start_location) {
    this.ID = id;
    this.role = role;
    this.location = start_location;
}

function Location(lat, long) {
    this.latitude = lat;
    this.longitude = long;
}

var DifferentRoles = function(result_list) {
    const random_role_name = "random"
    for(i = 0; i < result_list.length - 1; ++i) {
        for(j = i + 1; j < result_list.length; ++j) {
            if(result_list[i].role !== random_role_name &&
                result_list[i].role === result_list[j].role) {
                return false;
            }
        }
    }
    return true;
}

var CloseTogether = function(result_list) {
    for(i = 0; i < result_list.length - 1; ++i) {
        for(j = i + 1; j < result_list.length; ++j) {
            var location1 = result_list[i].location;
            var location2 = result_list[j].location;
            var distance = afar(location1.latitude, location1.longitude,
                                location2.latitude, location2.longitude);
            if(distance > 2 * radius) {
                    return false;
                }
            
            }
        }
    return true;
}

describe('Matchmaking fail', (user_list) => {
     //Arrange
     user_list = []
     var location1 = new Location(53.3416875, -6.6530796)
     var user1 = new User(1, "Defender", location1)
     var location2 = new Location(53.341523, -6.253912)
     var user2 = new User(2, "Attacker", location2)
     var location3 = new Location(-4, 6)
     var user3 = new User(3, "Support", location3)
     var location4 = new Location(53.1416123, -6.2530123)
     var user4 = new User(4, "Support", location4)
     user_list.push(user1);
     user_list.push(user2);
     user_list.push(user3);
     user_list.push(user4);

     //Act
     var result = Matchmaking(user_list)

    it('should return no users',
    () => {
        expect(result.length).to.be.equal(0);
    });
});

describe('Matchmaking pass', (user_list) => {
    //Arrange
    user_list = []
    var location1 = new Location(53.3416875, -6.2530796)
    var user1 = new User(1, "Defender", location1)
    var location2 = new Location(53.3416875, -6.253912)
    var user2 = new User(2, "Attacker", location2)
    var location3 = new Location(-4, 6)
    var user5 = new User(2, "Attacker", location3)
    var user3 = new User(3, "Support", location3)
    var location4 = new Location(53.3416875, -6.2530123)
    var user4 = new User(4, "Support", location4)
    user_list.push(user1);
    user_list.push(user2);
    user_list.push(user3);
    user_list.push(user4);
    user_list.push(user5);

    //Act
    var result = Matchmaking(user_list)

   it('should return three users',
   () => {
       expect(result.length).to.be.equal(3);
   });
   it('should return users with different roles',
   () => {
       expect(DifferentRoles(result)).to.be.equal(true);
   });
   it('should return users close together',
   () => {
       expect(CloseTogether(result)).to.be.equal(true);
   });
});
