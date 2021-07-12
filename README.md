# Travel Guide

---

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
A way for individuals to learn information about locations that they are traveling or living in.

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Travel
- **Mobile:** Mobile first experience, uses gps
- **Story:** Allow users to share hidden details about locations using photo, video, or audio
- **Market:** Anyone that travels who are curious about locations
- **Habit:** User can post about locations they traveled or lived in it won't be as frequent as social media. If you are traveling somewhere and you want to learn about fun places to visit you can check the app.
- **Scope:** The scope of the app will be initially small focusing on adding text, photo, and video descriptions, in the future we can start incorporating more richer data formats

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can create an account
* User can login/signup
* User can post a travel post for a location
    * can edit
* User can search for a location
* User can see others profile and see what posts they created
* Like travel guide


**Optional Nice-to-have Stories**
* Trending locations
* User can "follow" a location in case of updates and get notifications
* User can add a profile photo
* Buddy feature for users
* Users can add replies to a travel post

### 2. Screen Archetypes

* Login screen
   * User can login
* Signup screen
   * User can signup
* Stream
   * Map view showing different travel posts
* Detail
   * Travel post info
* Creation
   * post location (required)
   * text detail (required)
   * photo/video detail (optional)
* Search
   * User can search locations

### 3. Navigation

**Flow Navigation** (Screen to Screen)

* Login Screen
   * Home
* Registration Screen
   * Home
* Stream Screen
   * Travel info details
* Creation Screen
   * Home
* Search Screen
   * Detail screen
* Profile Screen
    * Home


## Wireframes
### Digital Wireframes
https://www.figma.com/file/EqXxfgaRjDbAISd6Vfr885/TravelGuide-Wireframe?node-id=1%3A779
### Digital Prototype
<iframe style="border: 1px solid rgba(0, 0, 0, 0.1);" width="800" height="450" src="https://www.figma.com/embed?embed_host=share&url=https%3A%2F%2Fwww.figma.com%2Fproto%2FEqXxfgaRjDbAISd6Vfr885%2FTravelGuide-Wireframe%3Fpage-id%3D0%253A1%26node-id%3D20%253A76%26viewport%3D1147%252C350%252C0.9509857892990112%26scaling%3Dscale-down" allowfullscreen></iframe>

## Schema
### Models
Guide
| Property | Type     | Description |
| -------- | -------- | --------    |
| author     | Pointer to User     | Guide author |
| location     | Parse GeoPoint     | Guide location |
| text     | String  | text that user adds |
| photo     | ParseFile  | image that user adds (optional)|
| audio     | ParseFile  | audio that user adds (optional)|
| video     | ParseFile  | video that user adds (optional)|
| likes     | Number  | number of likes that the guides receives|
| createdAt | DateTime  | date when post is created|
| updatedAt | DateTime  | date when post is edited|

User
| Property | Type     | Description |
| -------- | -------- | --------    |
| username     | String   | username of user |
| password     | String   | password of user |
| avatar     | ParseFile   | profile image of user (optional) |

### Networking
* Login screen
   * (Read/GET) Get current user instance
```java=
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    // TODO: better error handling
                    showLoginState(R.string.login_failed);
                    Log.e(TAG, "Issue with login", e);R
                    return;
                }

                Log.e(TAG, "Login success", e);
            }
        });
```
* Signup screen
   * (Create/POST) Create a new User object
* Stream
   * (Read/GET) Get all guides created by users
```java=
        ParseQuery<Post> query = ParseQuery.getQuery(Guide.class);
        // include data referred by user key
        query.include(Guide.getKeyUser());
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting guides", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat
                for (Guide guide : guides) {
                    Log.i(TAG, "Guide: " + Guide.getDesc() + ", username: " + post.getUser().getUsername());
                }
            }
        });
    }
```
* Detail
   * (Delete) Delete existing like
* Creation
   * (Create/POST) Create a new Guide
* Profile
   * (Read/GET) Get current user instance
   * (Update/PUT) Update user guides
   * (Delete) Delete existing guide


---
---
