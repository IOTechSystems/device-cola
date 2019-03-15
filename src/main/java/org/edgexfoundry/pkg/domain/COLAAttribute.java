/*******************************************************************************
 * Copyright 2016-2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice: device-cola
 * @author: Tyler Cox, Dell
 * @version: 1.0.0
 *******************************************************************************/

package org.edgexfoundry.pkg.domain;

import com.google.gson.Gson;
import org.edgexfoundry.support.logging.client.EdgeXLogger;
import org.edgexfoundry.support.logging.client.EdgeXLoggerFactory;

public class COLAAttribute {

  private static final EdgeXLogger logger =
	  EdgeXLoggerFactory.getEdgeXLogger(COLAAttribute.class);

  // Replace these attributes with the COLA
  // specific metadata needed by the COLA Driver
  
	private String Instance;
	private String Type;

  public COLAAttribute(Object attributes) {
    try {
      Gson gson = new Gson();
      String jsonString = gson.toJson(attributes);
      COLAAttribute thisObject = gson.fromJson(jsonString, this.getClass());
      
			this.setInstance(thisObject.getInstance());
			this.setType(thisObject.getType());

    } catch (Exception e) {
      logger.error("Cannot Construct COLAAttribute: " + e.getMessage());
    }
  }

  
	public String getInstance()
	{
		return Instance;
	}
	public String getType()
	{
		return Type;
	}

  
	public void setInstance(String Instance)
	{
		this.Instance = Instance;
	}
	public void setType(String Type)
	{
		this.Type = Type;
	}

}

