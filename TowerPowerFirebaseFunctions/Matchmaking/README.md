# Dependencies
See the package.json inside of the functions directory for extra packages I require. Furthermore, firebase dependencies are required. Again the package.json files specify these.

# Running
If node is installed and the function is not a firebase function, run:
> node "name_of_file.js"

If it is a firebase function it needs to be deployed using:
> firebase deploy --only functions:"name_of_function"

# Testing
Move into the tests directory and run:
> npm test
