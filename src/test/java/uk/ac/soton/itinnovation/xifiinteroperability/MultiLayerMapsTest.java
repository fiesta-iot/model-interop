/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2017
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.MultiTestsExecutionManager;

/**
 * Tests the multi layer maps in the multi-tests execution manager
 * 
 * @author ns17
 */
public class MultiLayerMapsTest {
    
   private final MultiTestsExecutionManager execManager = new MultiTestsExecutionManager();

    @Test
    public final void testThreeLayerMap() {
        execManager.resetMaps();
        Object val = execManager.getTestHeader("test", "label", "header");
        Assert.assertTrue("Null was not returned in case of a non existing link.", val == null);

        execManager.putTestHeader("test1.xml", "label1", "header1", "value1");
        Assert.assertEquals("Three-layer map doesn't return correct results or doesn't properly put results into the map",
                "value1", execManager.getTestHeader("test1", "label1", "header1"));

        Assert.assertTrue("Null was not returned in case of a non existing link, when put was used with another values.",
                execManager.getTestHeader("test", "label", "header") == null);
        
        execManager.putTestHeader("test", "label", "header", "value");
        execManager.putTestHeader("test", "state", "header1", "value1");
        Assert.assertEquals("Three-layer map doesn't return correct results or doesn't properly put results into the map",
                "value", execManager.getTestHeader("test", "label", "header"));
        
        execManager.removeAllTestHeaders("test1");
        Assert.assertTrue("Three-layer map doesn't return correct results after removing an entry", 
                execManager.getTestHeader("test1", "label", "header") == null);
        
        execManager.removeAllStateHeaders("test", "label");
        Assert.assertTrue("Three-layer map doesn't return correct results after removing an entry", 
                execManager.getTestHeader("test", "label", "header") == null);
        
        Assert.assertEquals("value1", execManager.getTestHeader("test", "state", "header1"));
        execManager.removeHeaderID("test", "state", "header1");
        Assert.assertTrue("Three-layer map doesn't return correct results after removing an entry", 
                execManager.getTestHeader("test", "state", "header1") == null);
    }

    @Test
    public final void testTwoLayerMaps() {
        execManager.resetMaps();
        Object val = execManager.getTestContent("test", "label");
        Assert.assertTrue("Null was not returned in case of a non existing link for the test content map.", val == null);
        
        val = execManager.getPatternValue("test", "id");
        Assert.assertTrue("Null was not returned in case of a non existing link for the test content map.", val == null);
        
        execManager.putTestContent("test1", "label1", "content1");
        execManager.putPatternValue("test1", "id1", "value1");
        Assert.assertEquals("Two-layer content map doesn't return correct results or doesn't properly put results into the map",
                "content1", execManager.getTestContent("test1.xml", "label1"));
        Assert.assertEquals("Two-layer pattern values map doesn't return correct results or doesn't properly put results into the map",
                "value1", execManager.getPatternValue("test1.xml", "id1"));
        
        Assert.assertTrue("Null was not returned in case of a non existing link, when put was used with another values.",
                execManager.getTestContent("test", "label") == null);
        Assert.assertTrue("Null was not returned in case of a non existing link, when put was used with another values.",
                execManager.getPatternValue("test", "id") == null);
        
        execManager.putTestContent("test", "label", "content");
        execManager.putPatternValue("test", "id", "value");
        Assert.assertEquals("Two-layer content map doesn't return correct results or doesn't properly put results into the map",
                "content", execManager.getTestContent("test", "label"));
        Assert.assertEquals("Two-layer pattern values map doesn't return correct results or doesn't properly put results into the map",
                "value", execManager.getPatternValue("test", "id"));
        
        execManager.removeAllTestContent("test");
        Assert.assertTrue("Two-layer content map doesn't return correct results after removing an entry", 
                execManager.getTestContent("test1", "label") == null);
        
        execManager.removeAllTestPatternValues("test");
        Assert.assertTrue("Two-layer pattern values map doesn't return correct results after removing an entry", 
                execManager.getPatternValue("test", "id") == null);
        
        execManager.putTestContent("test1", "label2", "content2");
        execManager.putPatternValue("test1", "id2", "value2");
        Assert.assertEquals("Two-layer content map doesn't return correct results or doesn't properly put results into the map",
                "content2", execManager.getTestContent("test1.xml", "label2"));
        Assert.assertEquals("Two-layer pattern values map doesn't return correct results or doesn't properly put results into the map",
                "value2", execManager.getPatternValue("test1.xml", "id2"));
        execManager.removeAllStateContent("test1", "label2");
        execManager.removePatternValueID("test1", "id2");
        Assert.assertTrue("Two-layer content map doesn't return correct results after removing an entry", 
                execManager.getTestContent("test1", "label2") == null);
        Assert.assertTrue("Two-layer pattern values map doesn't return correct results after removing an entry", 
                execManager.getPatternValue("test1", "id2") == null);
    }
}
