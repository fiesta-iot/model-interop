# Tutorial: Using the collections explorer of ModelInterop

### What is the collections explorer?
This is basically a file explorer providing the capability of working on multiple test models simultaneously. It provides features such as opening, removing, renaming and moving models within collections. Collections are effectively folders containing a number of models (stored in XML files). All of these collections are maintained in a folder representing your workspace (indeed, this folder is called workspace). Every modification or creation of models using the tool is maintained in the workspace.

### How is this visualised in the tool?
First important thing to note is that on loading of the tool, you will be asked to choose a location for your workspace folder. 

![Workspace location chooser][screenshot-71] 

When you choose the location you prefer, a folder called 'workspace' will be created. This represents the workspace you will use while developing models with the tool. If a folder with this name already exists, the tool will it instead of creating a new one. Then a default collection is created, also called 'workspace'. If you chose a workspace, which was used before and has other collections in it, they will also be loaded in the tool. After the workspace has been defined the tool will run displaying your collections in the explorer.

![Workspace visualization][screenshot-72] 

Another thing to keep in mind is that the explorer is effectively a file explorer. Any modification made in the tool would be present in the local file system. Moreover, while running the tool locks the entire workspace, so changes to files in the workspace folder could be made only through the collections explorer. This is to ensure isolation for the operations the tool performs on models.

### How to use the collections explorer?
***A simple demonstration of the major operations that could be performed using collections*** 

***

**Creating a new collection**
As we can see on the previous screenshot, in the beginning we only have one collection, which is created by default (also called 'workspace'). However, we encourage the user to use this collection only when experimenting with the tool or for self-independent models, which are not related to other models. So, first I am going to create a new collection called 'tests', where I will store the models for this tutorial. Click the right button on the 'Available collections' label and then choose the option 'Add new collection'.

![Add new collection][screenshot-73] 

The tool then asks for the name of the new collection. As I said above, we will call this collection tests.

![Name of collection][screenshot-74] 

Now, click OK and the collection will be visualized in the collections explorer. Keep in mind that the tool also creates an actual folder in the location you chose for workspace.

![Visualizing collection][screenshot-75] 

**Creating a new model within a collection**
Now, I am going to create the first model we will need for this tutorial. Usually, we create new models by clicking on the 'New file' icon (the one with the plus side on it). However, this will automatically create the new model in the collection for independent models, which is 'workspace'. Since we want to organize our models in the 'tests' collection, we can do the following: click the right button on the 'tests' collection and then choose the 'Open new model in this collection' option.

![Open new model][screenshot-76] 

Then a new xml model called 'new.xml' will be created within the 'tests' collection. This would also cause the same effect in the local file system by creating an empty xml file in the tests folder.

![New model created][screenshot-77] 

**Renaming the model**
Now, I don't want my new model to be called 'new.xml', since it doesn't give us any indication of what the model is about. Hence, let's rename it to something different. This tutorial will use the [Fixer](http://fixer.io/) API for one of the example compliance test models. Therefore, I will rename my model to 'fixer.xml' Click the right button on the 'new.xml' model and choose the 'Rename this model' option.

![Renaming model][screenshot-78] 

You will be prompted to choose a new name for the model. Type 'fixer' or 'fixer.xml'. The '.xml' extension is optional, so both will work. Then press OK.

![Model new name][screenshot-79] 

The model will now be renamed to 'fixer.xml'.

![Model renamed][screenshot-80] 

**Implementing the model**
Since this tutorial is about working with the collections explorer, I am not going to dive into the details of implementing test models and will create a very simple one just for the usage of collections. If you want to know more details about the process of creating complex models, please check the [documentation](https://iglab.it-innovation.soton.ac.uk/iot/connect-iot/blob/public/README.md). For the fixer model, we will use three states: a 'triggerstart', a 'normal' and an 'end' state. So drag one of each into the test state model.

![Behaviour graph states][screenshot-81] 

Now, connect the 'triggerstart' node to the 'normal' node and the 'normal' node to the 'end' node.

![Transitions][screenshot-82] 

Now, let's implement the deployment model. In the top-left corner, choose the 'Deployment' tab and drag an 'HTTP Interface' node into the deployment model.

![Deployment node][screenshot-83] 

After the model is built, we need to feed it with data. First, click on the interface node we dragged in the last step. In the host address text box, fill api.fixer.io and click the update button. Now, add an interface. We can use 'rest' for name, while for address we will use https://api.fixer.io:443/latest. To add it, click on the 'Add' button.

![Filling data to deployment node][screenshot-84] 

The next step is to add some model variables. Click on the triggerstart node and add two model variables:
1) First with name 'base' and value 'GBP'
2) Second with name 'city' and value 'London' (This will come in handy when implementing the second model)

![Adding model variables][screenshot-85] 

Then click on the 'end' state and update its 'success' value to true. The test report could be left empty for this example. Don't forget to click on the 'Update end state' button.

![Adjusting end state][screenshot-86] 

Now we update the transition between the 'triggerstart' and the 'normal' node. Click on this transition. For interface target, we have only one option (the interface we added earlier) and, hence, we choose it. For resource path, we are going to use one of the model variables. So set the resource path to '?base=\$$patterndata.base\$$'. Set the request method to 'GET'. Although we won't use a message body for this request, we will set the data type to JSON. Then we click on the 'Update message' button.

![Filling data to trigger transition][screenshot-87] 

We will also add a header 'Content-Type' with value 'application/json' since the Fixer API uses JSON format for representing data. Fill it and click the 'Add header' button.

![Adding a header in the trigger message][screenshot-88] 

Now, we will update the guard transition, which is the transition between the 'normal' and the 'end' state. Click on it and let's add the following guards:
1) Function: equal, Parameter to test: http.code, Required value: 200
2) Function: equal, Parameter to test: http.from, Required value: component.interface.address
3) Function: equal, Parameter to test:http.msg, Required value: REPLY
4) Function: lessthan Parameter to test: response-time, Required value: 1000

![Adding guards][screenshot-89] 

We are pretty much done with this model, so we can now click the save icon to record this in the actual XML file.

![Saving the model][screenshot-90] 

Now, let's just run the test once to check if we haven't made any errors.

![Running the test][screenshot-91] 

If no errors were made, we should see a successfully ran test. Keep in mind that the test might fail if the Fixer API is overloaded with requests, and cannot give a response within 1 second in the given moment. This, however, doesn't indicate an error in the test model.

![Successful test][screenshot-92] 

Now, I am going to create a second model - as simple as the first one, and I will use it to demonstrate the use of referencing data from a previously executed test in another test. Briefly explained, let's say you have Test A and Test B in a collection and you run them both. If Test A is the test that is executed first, then we can use pretty much all the captured data from it by referencing it in Test B. Referencing information applies for model variables, captured event headers and captured event content. More on this later.

First, let's create our second compliance test model. We will use the [Google Maps Geocoding API](https://developers.google.com/maps/documentation/geocoding/intro). We follow the same process as in the previous example.

***Create a new model in the tests 'collection'***  

![New model][screenshot-93] 

***Rename this model to 'geocode'***  

![Rename model][screenshot-94] 

***Open this model for editing***  

![Open model][screenshot-95] 

The pattern of this model will be the same as the one in the previous example - one HTTP interface node in the deployment model and three states in the test state model - a 'triggerstart' node, a 'normal' node and an 'end' node.

![Implementing model][screenshot-96] 

Following the same pattern, now, we will again create one transition from the 'triggerstart' node to the 'normal' node and another one from the 'normal' node to the 'end' node.

![Adding transitions][screenshot-97] 

Now, we will fill the test data. We start with the interface node.
Host address for this interface is: maps.googleapis.com (Don't forget to click the update button)
Only 1 interface URL is added with name: rest and URL: https://maps.googleapis.com:443/maps/api/geocode/json

![Data for the interface node][screenshot-98] 

Then, we fill the data for the first transition - from triggerstart to normal. Here, I will demonstrate how to **reference model variables from a previous test**.

For interface target, we have only one option, which is the interface URL we filled in.
We will do a GET request, so method is GET.
The type of data we are using is JSON.
For resource path, we will use a model variable from the fixer test. The syntax for referencing model variables is:
**\$$test.{test-id}.patterndata.{data-id}\$$** where we replace *{test-id}* with the name of the test and *{data-id}* with the name of the model variable. In this case, test-id is fixer, while the model variable we will be using is city (the second model variable we created in the fixer model). Hence, the syntax we use is **\$$test.fixer.patterndata.city\$$** and the full resource path will be **?address=\$$test.fixer.patterndata.city\$$**

![Trigger transition data][screenshot-99] 

Next, we will add a header for the content type to the same transition. However, instead of manually typing 'application/json', I will demonstrate how to **use a header value captured in a previous test event**. Basically, we will use the content-type header captured in event 'normal' from the fixer test. The syntax for doing this is similar: **\$$test.{test-id}.{label-id}.headers|{header-id}\$$** where we replace *{test-id}* with the name of the test, *{label-id}* with the label of the event, from which we are fetching the header and *{header-id}* with the name of the header we want to use. In this case, test-id is fixer, label-id is normal and header-id is http.content-type. Hence, the syntax we use is **\$$test.fixer.normal.headers|http.content-type\$$**. So we add a header with name Content-Type and value the syntax we built.

![Header for trigger transition][screenshot-100] 

Finally, we need to fill some guards for the guard transition between state 'normal' and state 'end'. We start with the basic ones:
1) Function: equal, Parameter to test: http.code, Required value: 200
2) Function: equal, Parameter to test: http.from, Required value: component.interface.address
3) Function: equal, Parameter to test:http.msg, Required value: REPLY

![Adding basic guards][screenshot-101] 

The next guard we add will **use content captured in a previous test event**. We will test that the date format of the http header in the geocode API response is not the same as the date format in the content of the fixer API response. Hence, the function we use is **notequal**. The parameter to test is **http.date**. The guard value will use the syntax to reference content from a previous test event. The syntax is: 
**\$$test.{test-id}.{label-id}.content|{xpath/jsonpath}\$$** 
In this case, *test-id* is fixer, *label-id* is normal. We use json path since the fixer API uses JSON as its data type. The json path we need is *date* . Therefore, the guard value is **\$$test.fixer.normal.content|date\$$** 
Please note that when using json path, the root part is omitted - we use **date**, instead of **$.date**.

![Adding a special guard][screenshot-102] 

Once more, we also update the end state success value to true and set it to an empty test report.

![Adjusting end state][screenshot-103] 

Now, the second model is also finished, and we click the 'save' button.

![Saving model][screenshot-104] 

In order to run both tests in the collection, click the right button of the mouse on the 'tests' collection and then choose "Run all tests in this collection"

![Running both tests][screenshot-105] 

You will be asked to run all tests in execution mode and without being asked to continue after one test has finished, that is not in debug mode. Choose **Yes**.

![Choosing execution mode][screenshot-106] 

Then you will see the tool executing the two tests. In the end, you will be asked to view the summary test report. Choose **Yes** again.

![View summary test report][screenshot-107] 

Finally, you should see the summary test report indicating that no failures were detected.

![Successful test report][screenshot-108] 

### Conclusions

In this tutorial, you learned how to use the collections explorer and how to reference data when doing multiple tests execution. The examples shown had no real practical application and were used only for the purpose of the tutorial. However, using data from one test in another can be extremely useful in some circumstances. For instance, running a collection of tests, which all use the same authentication token, can be modeled so that the token is obtained only in the first test and shared with the other tests.
Using the collections explorer can help us break up many tests into a small number of tests, which share a common functionality. It is a perfect way to manage your test models and gives you the valuable opportunity to run multiple tests at once, instead of manually running them one by one.

***

### Other useful features

* Moving model from one collection to another collection  

![Moving model][screenshot-109] 

* Open a model in a chosen collection  

![Opening model in collection][screenshot-110] 

* Save a collection  

![Saving collection][screenshot-111]   

* Open a collection - loads a folder with XML models in the tool

![Opening a collection][screenshot-112]

* Run all the tests in the available collections  

![Running all tests][screenshot-113]   

* Run only a number of tests in the order they were chosen

  While navigating on the collections explorer, press and hold the Ctrl key. Then choose the tests you want to run in the order you   want them being executed. You will be asked to confirm your choice.

![Running selected tests][screenshot-114]   



[screenshot-71]: /src/main/resources/images/screenshot-71.png "Workspace location chooser"
[screenshot-72]: /src/main/resources/images/screenshot-72.png "Workspace visualization"
[screenshot-73]: /src/main/resources/images/screenshot-73.png "Add new collection"
[screenshot-74]: /src/main/resources/images/screenshot-74.png "Name of collection"
[screenshot-75]: /src/main/resources/images/screenshot-75.png "Visualizing collection"
[screenshot-76]: /src/main/resources/images/screenshot-76.png "Open new model"
[screenshot-77]: /src/main/resources/images/screenshot-77.png "New model created"
[screenshot-78]: /src/main/resources/images/screenshot-78.png "Renaming model"
[screenshot-79]: /src/main/resources/images/screenshot-79.png "Model new name"
[screenshot-80]: /src/main/resources/images/screenshot-80.png "Model renamed"
[screenshot-81]: /src/main/resources/images/screenshot-81.png "Behaviour graph states"
[screenshot-82]: /src/main/resources/images/screenshot-82.png "Transitions"
[screenshot-83]: /src/main/resources/images/screenshot-83.png "Deployment node"
[screenshot-84]: /src/main/resources/images/screenshot-84.png "Filling data to deployment node"
[screenshot-85]: /src/main/resources/images/screenshot-85.png "Adding model variables"
[screenshot-86]: /src/main/resources/images/screenshot-86.png "Adjusting end state"
[screenshot-87]: /src/main/resources/images/screenshot-87.png "Filling data to trigger transition"
[screenshot-88]: /src/main/resources/images/screenshot-88.png "Adding a header in the trigger message"
[screenshot-89]: /src/main/resources/images/screenshot-89.png "Adding guards"
[screenshot-90]: /src/main/resources/images/screenshot-90.png "Saving the model"
[screenshot-91]: /src/main/resources/images/screenshot-91.png "Running the test"
[screenshot-92]: /src/main/resources/images/screenshot-92.png "Successful test"
[screenshot-93]: /src/main/resources/images/screenshot-93.png "New model"
[screenshot-94]: /src/main/resources/images/screenshot-94.png "Rename model"
[screenshot-95]: /src/main/resources/images/screenshot-95.png "Open model"
[screenshot-96]: /src/main/resources/images/screenshot-96.png "Implementing model"
[screenshot-97]: /src/main/resources/images/screenshot-97.png "Adding transitions"
[screenshot-98]: /src/main/resources/images/screenshot-98.png "Data for the interface node"
[screenshot-99]: /src/main/resources/images/screenshot-99.png "Trigger transition data"
[screenshot-100]: /src/main/resources/images/screenshot-100.png "Header for trigger transition"
[screenshot-101]: /src/main/resources/images/screenshot-101.png "Adding basic guards"
[screenshot-102]: /src/main/resources/images/screenshot-102.png "Adding a special guard"
[screenshot-103]: /src/main/resources/images/screenshot-103.png "Adjusting end state"
[screenshot-104]: /src/main/resources/images/screenshot-104.png "Saving model"
[screenshot-105]: /src/main/resources/images/screenshot-105.png "Running both tests"
[screenshot-106]: /src/main/resources/images/screenshot-106.png "Choosing execution mode"
[screenshot-107]: /src/main/resources/images/screenshot-107.png "View summary test report"
[screenshot-108]: /src/main/resources/images/screenshot-108.png "Successful test report"
[screenshot-109]: /src/main/resources/images/screenshot-109.png "Moving model"
[screenshot-110]: /src/main/resources/images/screenshot-110.png "Opening model in collection"
[screenshot-111]: /src/main/resources/images/screenshot-111.png "Saving collection"
[screenshot-112]: /src/main/resources/images/screenshot-112.png "Opening collection"
[screenshot-113]: /src/main/resources/images/screenshot-113.png "Running all tests"
[screenshot-114]: /src/main/resources/images/screenshot-114.png "Running selected tests"
