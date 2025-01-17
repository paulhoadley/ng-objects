# ng-objects

ng-objects is an open source exploration of Apple's WebObjects (WO) framework. It aims to:

1. Extract the best concepts of WO and use them in a new, modern library.

2. Be compatible with WO's deployment environment, so apps can transparently integrate into an existing WO based deployment infrastructure. This means the apps know how to speak to JavaMonitor/wotaskd and handle requests from the WO Apache adaptor.

3. Not be a WO clone but _familiar_ to a WO programmer and thus easy to move existing WO code to. While WO is great, there have been two decades of improvements in software design and web development since WO's last official release.

## parts (TODO)

* WO webserver adaptor compatibility
* wotaskd and JavaMonitor compatibility
* More deployment goodies (application lifecycle management)
* Basic Request handling and HTTP apis
* Property loading
* Session management
* Component actions
* Components/Templating/Dynamic elements/Associations
* KVC
* Resource management

## How do I launch `ng-testapp`?

1. You'll need [vermilingua-maven-plugin](https://github.com/undur/vermilingua-maven-plugin).
    1. Clone that repo.
    2. Run `mvn install`.
2. Install the `ng-objects` dependencies.
    1. Clone this repo.
    2. Run `mvn install` at the top level.
3. Build and launch `ng-testapp`:
    1. `cd ng-testapp`
    2. `mvn package`
    3. `./target/ng-testapp-1.0.0-SNAPSHOT.woa/ng-testapp`
4. Point your browser at `localhost:1200`.

The _expected output_ is `Welcome to NGObjects! Sorry, but I have no idea how to handle requests without path elements`. Congratulations!
