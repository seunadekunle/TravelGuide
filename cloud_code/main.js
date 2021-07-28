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
                sendNotifications(followed);
            }
        })
        .catch(error => {
            console.log(error);
        });



    function sendNotifications(followed) {

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
