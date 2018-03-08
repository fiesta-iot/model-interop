## Tutorial: Using ModelInterop to create an Interoperability Test Model

### System Under Test
For this tutorial we present a simple "hello world" style example to illustrate
how to perform an interoperability test. The important concept is that this
is a test of two or more systems who are exchanging messages to perfom a function.
For example, in the figure below there are two systems: a Web Service and
a Web Client. We will use this example for the remainder of the tutorial

![Interoperability test system][system] 

#### Random number addition API and client
The simple example we use for this tutorial is a trivial REST API that
provides two operations:

* A method to retrieve a random integer value.
* A method to add a constant value (e.g. 5) to a retrieved random value.

To help build such a service, we provide a Swagger API specification that can
be used to generate the service in the Web container of your choice. The
Swagger is available at [Interoperability Test API] (/src/main/resources/examples/random.yaml).

Using the Swagger editor, you can then generate the complete service code template [website] (http://editor.swagger.io).

We provide example JAX-RX code to fill in the template functionality. First the 
random number method implementation:

```java
public class NumberApiServiceImpl extends NumberApiService {

    private int counter = 0;
    private int upper = 125;
    private int lower = 1;

    @Override
    public Response numberGet(SecurityContext securityContext) throws NotFoundException {
        int r = (int) (Math.random() * (upper - lower)) + lower;
        String json = "{\n \"id\": \"" + counter++ + "\",\n \"value\": \"" + r + "\"\n" + "}";
        return Response.ok().entity(json).build();
    }
}
```

Then the addition method implementation:

```java
public class AddApiServiceImpl extends AddApiService {

    @Override
    public Response addGet(io.swagger.model.Number input, SecurityContext securityContext) throws NotFoundException {
        String id = input.getId();
        int value = 5 + Integer.valueOf(input.getValue());
        String result = "{\"id\": \"" + id + "\",\"result\": \"" + value + "\"}";
        return Response.ok().entity(result).build();
    }
}
```

We then need a client application that will call these two methods on the service. We
provide a sample Java main method implementation that uses the Jersey Rest
Client to make invocations on the Web Service:

```java
public class RandomClient {

    public static void main(String[] args) {

        String url = "http://localhost:8087/random";

        /**
         * Get a number using HTTP GET call
         */
        Client client = Client.create();
        WebResource numberService = client.resource(url + "/number");
        ClientResponse numberServiceResponse = numberService.get(ClientResponse.class);

        String response = helloServiceResponse.getEntity(String.class);
        id = JsonPath.read(response, "$.id");

        /**
         * Add a number using HTTP POST call
         */
        WebResource addService = client.resource(url + "/add");
        ClientResponse addServiceRespnse = addService.type("application/json").post(ClientResponse.class, response);
        System.out.println("Output from Server: " + addServiceRespnse.getEntity(String.class));
     }
}
```

Note, that the client points to the network address endpoint - localhost:8087 defined
in the swagger. This states the web service is hosted on localhost and is listening on
port 8087 for the HTTP requests.

#### Create an Interoperability Test

* **Creating a deployment model**

The first step in creating an interoperability test model, is to model the two
elements being observed in the deployment section of the tool. First, we
model the http service as follows. Drag the HTTP icon from the deployment tab
palette at the left of the tool into the deployment model section. Then model the client
application by dragging the Client icon to the same location. The screen should
look as follows.

![Creating the deployment model][interop-1]

Next we fill in the form data for the service and the client. For the service it is:
* Component name: service
* Component address: 127.0.0.1

Click the update button 

* Url name: random
* Url URL: http://localhost:8087/random

Click the add URL button.

![Specify the deployment information][interop-2]

Then click on the client in the model, and fill in the information. For this, we
fill in only the name and address (there are no interfaces).

* Component name: client
* Component address: 127.0.0.1

Click the update button 

![Specify the deployment information][interop-3]

* **Creating a behaviour model**

Now we specify the behaviour model which describes the observed behaviour
we want to test. First we want to test that the client sends a HTTP GET message
to the service correctly. We model this as follows:

* Drag a start node from the palette to the behaviour model (this is the normal start and is not a trigger start, we are observing an event and not stimulating the event).
* Drag a normal node from the palette to the model.
* Connect the start node to the normal node.

![Model the GET call from the client][interop-4]

Next we add a simple test to this transition, enter the following information:
* Function: equal
* Parameter: http.msg
* Value: GET

This will test that the client sends a HTTP Get message to the service.

![Simple HTTP test][interop-5]

Next we want to test that the server sends a valid response to the client. So
we add to the model:

* Drag a normal node to the model (state1) and connect normal to state1.

Then add the following rules to the transition (these check that the http response
message is correct and the json body has the correct fields):

* http.code = 200
* http.msg = reply
* content[$] contains id
* content[$] contains value

![HTTP response test][interop-6]

We define another two states for the next call for the addition POST method:

* Drag a normal node to the model (state2) and connect state1 to state2.
* Add a test rule to check the client sends a HTTP POST message to the service.

![HTTP post test][interop-7]

* Drag an end node to the model (end) and connect state2 to end.
* Add test rules to check the response is correct and the json content contains valid data.

![HTTP post response test][interop-8]

We have now finished the complete interoperability model, and we can test the client
and service interoperate correctly with one another.

## Run an interoperability test
As with the compliance test, we run a test in the same way. Select the "Run test"
icon from the toolbar and the test will be started.

![Test report][interop-9]

The above screen shows that the test is now waiting for something to happen. We need
to make the client send a request to the service. However, we need to alter the
client code to make it work with the test environment. The client needs to point
to a proxy of the service that is auto-generated by the tool. Every request to this
proxy is redirected to the service. Note this is the method employed to observe messages
without monitoring packets on the network.

The highlighted box (in red) shows the address of this proxy. It is the address of the tool (localhost in this case)
and the proxy port (here 8088 is shown).

So we change the client code to (just a change to the URL):

```java
public class RandomClient {

    public static void main(String[] args) {

        String url = "http://localhost:8088/random";

        /**
         * Get a number using HTTP GET call
         */
        Client client = Client.create();
        WebResource numberService = client.resource(url + "/number");
        ClientResponse numberServiceResponse = numberService.get(ClientResponse.class);

        String response = helloServiceResponse.getEntity(String.class);
        id = JsonPath.read(response, "$.id");

        /**
         * Add a number using HTTP POST call
         */
        WebResource addService = client.resource(url + "/add");
        ClientResponse addServiceRespnse = addService.type("application/json").post(ClientResponse.class, response);
        System.out.println("Output from Server: " + addServiceRespnse.getEntity(String.class));
     }
}
```

If we now execute this client code, the test will observe the interactions between
the client and the service and show the test report as follows.

![Test complete][interop-10]

## Conclusion

In this tutorial, we have shown you how you can create specification models and perform interoperability tests using the 
tool. While this is a simple example modelling the interactions between two parties, the tool can model distributed systems
of hundreds of elements and check that they interoperate correctly. 

[system]: src/main/resources/images/system.png "System under test"
[interop-1]: src/main/resources/images/interop1.png "Deployment model"
[interop-2]: src/main/resources/images/interop2.png "Deployment form"
[interop-3]: src/main/resources/images/interop3.png "Deployment form"
[interop-4]: src/main/resources/images/interop4.png "Behaviour model"
[interop-5]: src/main/resources/images/interop5.png "Behaviour test Get"
[interop-6]: src/main/resources/images/interop6.png "Get response"
[interop-7]: src/main/resources/images/interop7.png "Post msg"
[interop-8]: src/main/resources/images/interop8.png "Complete model"
[interop-9]: src/main/resources/images/interop9.png "Display proxy"
[interop-10]: src/main/resources/images/interop10.png "Complete test"
