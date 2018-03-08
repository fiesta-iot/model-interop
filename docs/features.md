## Other Tool Features

So far, you've explored the tool by creating an interoperability model, executing interoperability tests and viewing test reports. Now, we are going to 
guide you through some additional features of the tool. First, we open the Geocode example available from the "examples" folder of this repository. 

#### XML view of the pattern
The tool allows you to view the data of your model in xml format (the xml file that will be generated when you save your pattern will 
contain this data) and even partially modify it. Click on the XML view icon and then you should see the XML view panel. 

![XML view][screenshot-45]  

On the top you should see the **Enable pattern editing** button and the **Editing legend**, which I am going to explain in a moment. First, 
click on the **Enable pattern editing** button. 

![Editing XML pattern][screenshot-46]  

Now, you should see two new buttons: **Disable pattern editing** and **Update changes**. All changes made in the XML view will only be 
updated on the actual pattern if you click on **Update changes**. You should also see that there are highlighted XML tags with different colors.

![Editing XML mode][screenshot-47]  

As explained in the legend, clicking on a light-orange tag will let you replace the text value. For instance, let's click 
on the **City** value and try to change it to something else.  

![Editing XML mode][screenshot-48]  

You should see a dialog asking you to type the new value. For this example, let's type **Berlin** and then click **OK**.  

Now, the value should be changed to **Berlin**. 

![Changed pattern data][screenshot-50]  

Click on the **Update changes** button so that the change is validated and updated. Now let's try to append data. Click 
on **Enable pattern editing** again. As explained in the legend, clicking on purple tag appends data. Let's click on the **patterndata** tag. You will be asked to confirm your choice, click **Yes**.

![Adding pattern data][screenshot-51]  

You should now see a dialog for filling the ID of the new pattern data.  

![id for pattern data][screenshot-52]  

For ID type **testID**. Then you will be asked to enter value for the new pattern data. Type **testValue** and click **OK**. You should 
now see that the new pattern data is appended in the XML.  

![appended pattern data][screenshot-53]  

Update the change again by clicking on **Update changes**. Now go back to the graph view and click on the **triggerstart** node. You should 
see that the pattern data is updated.  

![updated data][screenshot-54]  

For the sake of the example, let's now delete the newly added pattern data through XML editing. Go back to the XML view, **Enable pattern editing** and 
click on the second **data** tag. It is red, which, as explained in the legend, means that it will be deleted on click.

![delete data][screenshot-55]  

You will be asked to confirm that you want to delete the pattern data with ID **testID**. Click **Yes**. Then click on **Update changes** again. The 
second pattern data block will be removed.  
    
**Keep in mind** that whenever you do a replace edition - you replace the text value of something - you should update the changes and then continue 
editing the XML view if appropriate.

#### XPath expression generator

There are many cases in which it is not easy to track and derive the full XPath for some tag in XML data. The XPath expression generator will do that for you. You 
just have to load an XML file and then click on the tag, for which you want to generate the XPath expression. 

To open the tool, go to the **Tools** option from the toolbar and choose **XPath expression generator**.  

![xpath generator][screenshot-57]  

You will have to choose your XML file. I chose the XML file of the model we created in the previous steps.  

You can click on the **Highlight tags** button for easier navigation. Now click on some tag, text or attribute. We click on the url address 
for the **rest1** URL interface.  

![generating xpath][screenshot-59]

You should now see the generated XPath expression in a confirmation dialog asking whether you want to copy the XPath to the clipboard in order that it
can be used in the future by the tool.  

#### JSONPath expression generator

The JSONPath expression generator is pretty much the same as the XPath expression generator with the difference being that it works for 
JSON data format. Again, you load a JSON file and then click on the key or value, for which you want to generate the JSONPath expression.  
To open the tool go to the **Tools** option from the toolbar and choose **JSONPath expression generator**.  

You will have to choose your JSON file. I chose a simple JSON file I wrote as an example.  
 
You can click on the **Highlight keys and values** button for easier navigation. Now click on some key or value.  

You should now see the generated JSONPath expression in a confirmation dialog asking whether you want to copy the JSONPath.  
![generating jsonpath][screenshot-64]  


[undo]: https://iglab.it-innovation.soton.ac.uk/iot/connect-iot/raw/ui-update/src/main/resources/images/undo.gif "undo"
[redo]: https://iglab.it-innovation.soton.ac.uk/iot/connect-iot/raw/ui-update/src/main/resources/images/redo.gif "redo"
[cut]: https://iglab.it-innovation.soton.ac.uk/iot/connect-iot/raw/ui-update/src/main/resources/images/cut.gif "cut"

[screenshot-45]: /src/main/resources/images/screenshot-45.png "XML view"
[screenshot-46]: /src/main/resources/images/screenshot-46.png "XML view"
[screenshot-47]: /src/main/resources/images/screenshot-47.png "XML editing"
[screenshot-48]: /src/main/resources/images/screenshot-48.png "XML editing"
[screenshot-49]: /src/main/resources/images/screenshot-49.png "XML editing"
[screenshot-50]: /src/main/resources/images/screenshot-50.png "edited pattern data"
[screenshot-51]: /src/main/resources/images/screenshot-51.png "adding pattern data"
[screenshot-52]: /src/main/resources/images/screenshot-52.png "id for pattern data"
[screenshot-53]: /src/main/resources/images/screenshot-53.png "appended pattern data"
[screenshot-54]: /src/main/resources/images/screenshot-54.png "updated pattern data"
[screenshot-55]: /src/main/resources/images/screenshot-55.png "delete pattern data"
[screenshot-56]: /src/main/resources/images/screenshot-56.png "deleted pattern data"
[screenshot-57]: /src/main/resources/images/screenshot-57.png "xpath generator"
[screenshot-58]: /src/main/resources/images/screenshot-58.png "xpath generator"
[screenshot-59]: /src/main/resources/images/screenshot-59.png "xpath generator"
[screenshot-60]: /src/main/resources/images/screenshot-60.png "xpath generator"
[screenshot-61]: /src/main/resources/images/screenshot-61.png "jsonpath generator"
[screenshot-62]: /src/main/resources/images/screenshot-62.png "jsonpath generator"
[screenshot-63]: /src/main/resources/images/screenshot-63.png "jsonpath generator"
[screenshot-64]: /src/main/resources/images/screenshot-64.png "jsonpath generator"
[screenshot-65]: /src/main/resources/images/screenshot-65.png "adding client"
[screenshot-66]: /src/main/resources/images/screenshot-66.png "undo addition"
[screenshot-67]: /src/main/resources/images/screenshot-67.png "undo addition"
[screenshot-68]: /src/main/resources/images/screenshot-68.png "redo addition"
[screenshot-69]: /src/main/resources/images/screenshot-69.png "cut"
[screenshot-70]: /src/main/resources/images/screenshot-70.png "cut"