const { async } = require("parse/lib/node/Storage");

// references to the different Parse classes
const Location = Parse.Object.extend("Location");
const Guide = Parse.Object.extend("Guide");


// sends the follos notification
Parse.Cloud.define("sendFollowNotification", function (request) {

    // gets variables from parameters
    var locationID = request.params.locationID;
    var locationName = request.params.locationName;
    var userID = request.params.userID;

    console.log(locationID);
    console.log(locationName);
    console.log(userID);

    // references to the different Parse classes
    const Location = Parse.Object.extend("Location");
    const Activity = Parse.Object.extend("Activity");

    // Find location being sent
    const query = new Parse.Query(Location);

    // change this
    query.equalTo("objectId", locationID);
    query
        .find()
        .then(results => {

            var followed;

            results.forEach(result => {
                console.log(result.get("placeID"));
                followed = result;
            });

            // if there are users following the usersuser is
            if (typeof followed != 'undefined') {
                getFollowingUsers(followed);
            }
        })
        .catch(error => {
            console.log(error);
        });



    function getFollowingUsers(followed) {

        // test console
        console.log(JSON.parse(JSON.stringify(followed.get("coordinates"))))

        const followQuery = new Parse.Query(Activity);
        followQuery.select("userID");
        followQuery.select("locationID");
        followQuery.equalTo("locationID", followed);
        followQuery
            .find()
            .then(follows => {

                var users = [];
                console.log(follows.length)

                follows.forEach(follow => {

                    var followingUserId = follow.attributes.userID.id;

                    // if the user id is valid and the user wasn't the one that added the new post
                    if (typeof followingUserId != 'undefined' && followingUserId != userID) {
                        users.push(followingUserId);
                    }
                });

                // if there are users following this location
                if (users.length > 0) {
                    pushNotifications(users);
                }
            })
            .catch(error => {
                console.log(error);
            });
    }

    function pushNotifications(users) {

        // ensures that it only selects the users in the array
        const userQuery = new Parse.Query(Parse.User);
        userQuery.containedIn('objectId', users);

        // Find devices associated with these users
        const pushQuery = new Parse.Query(Parse.Installation);
        pushQuery.matchesQuery('userID', userQuery);
        Parse.Push.send({
            where: pushQuery,
            data: {
                title: "New Guide",
                alert: "A new guide has been added to " + locationName + ".",
            }
        }, { useMasterKey: true });

    }
    return "Notification Sent";
});

async function getLocations(lat, long) {

    var locationsData = [];

    // Find location being sent
    const locationQuery = new Parse.Query(Location);

    if (typeof lat != 'undefined' && typeof long != 'undefined') {
        locationQuery.withinMiles("coordinates", new Parse.GeoPoint(lat, long), 50);
        console.log("close location");
    }

    locations = await locationQuery.find();
    locations.forEach(location => {

        // creates new object
        let newLocation = new Object();

        newLocation.id = location.id;
        newLocation.followers = location.attributes.followers;
        newLocation.numGuides = 0;
        newLocation.numLikes = 0;
        newLocation.location = location;
        addNewLocation(newLocation);
    });

    // add new locations to the array
    function addNewLocation(newLocation) {
        locationsData.push(newLocation);
    }

    return locationsData;
}

async function getFinalLocations(lat, long) {

    var finalLocations = await getLocations(lat, long);
    console.log(finalLocations.length);

    for (var i = 0; i < finalLocations.length; i++) {

        // Find Guides that have been posted to the location
        const guideQuery = new Parse.Query(Guide);
        guideQuery.equalTo("locationID", finalLocations[i].location);
        let guides = await guideQuery.find();


        guides.forEach(guide => {
            finalLocations[i].numGuides = guides.length;

            // gets the number of likes that has been added to this location
            var likesTotal = 0;
            guides.forEach(guide => {
                likesTotal += guide.attributes.likes;
            });

            finalLocations[i].numLikes = likesTotal;
            // console.log(finalLocations[i]);
        });

    }

    return finalLocations;
}


// returns a list of trending locations
Parse.Cloud.define("getTrendingLocations", async (request) => {

    var lat = request.params.locationLat;
    var long = request.params.locationLong;

    console.log(lat);
    console.log(long);

    var topLocations = await getFinalLocations(lat, long);

    // removes locations with no followers, post or likes
    topLocations = topLocations.filter(topLocation => (topLocation.followers > 0 || topLocation.numGuides > 0 || topLocation.numLikes > 0));

    for (var i = 0; i < topLocations.length; i++) {
        console.log(topLocations[i]);
    }



    for (var i = 0; i < topLocations.length; i++) {

        // removes the location variable as it isn't needed
        delete topLocations[i].location;

        /// calculates rank for each of the locations
        var rank = (0.35 * topLocations[i].followers)
            + (0.5 * topLocations[i].numGuides) + (0.15 * topLocations[i].numLikes)

        topLocations[i].rank = rank;

        delete topLocations[i].followers;
        delete topLocations[i].numGuides;
        delete topLocations[i].numLikes;
    }

    // sorts the locations by the rank variable
    topLocations.sort((a, b) => (a.rank >= b.rank) ? -1 : 1)

    for (var i = 0; i < topLocations.length; i++) {
        console.log(topLocations[i]);
    }


    // top 3 locations
    return topLocations.splice(0, 3);
});


//    console.log(results.length);

//
//    // gets only the user column
//    query.select("userID")
//
//    query.exists("locationID")

//
//    // Find devices associated with these users
//    const pushQuery = new Parse.Query(Parse.Installation);
//    pushQuery.matchesQuery('userID', query);


//    return Parse.Push.send({
//            data: {
//                title: "New Guide",
//                alert: "A new guide has been added to one of your followed locations.",
//            }
//       }, { useMasterKey: true });

// return Parse.Push.send({
//        channels: ["News"],
//        data: {
//            title: "Hello from the Cloud Code",
//            alert: "Back4App rocks!",
//        }
//    }, { useMasterKey: true });

//Parse.Cloud.define("pushsample", (request) => {
//
//    return Parse.Push.send({
//        channels: ["News"],
//        data: {
//            title: "Hello from the Cloud Code",
//            alert: "Back4App rocks!",
//        }
//    }, { useMasterKey: true });
//});
