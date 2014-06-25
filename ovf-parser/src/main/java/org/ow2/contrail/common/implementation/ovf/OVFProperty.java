package org.ow2.contrail.common.implementation.ovf;

import java.util.HashMap;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.ow2.contrail.common.implementation.application.ApplianceDescriptor;

public class OVFProperty
{
	private String key;
	private String value;
	private Label label;
	private Description description;
	private boolean userConfigurable;
	private boolean password;
	private String type;
	private String qualifiers;

	
	
	public String getKey()
	{
		return this.key;
	}

	public String getValue()
	{
		return this.value;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public void setLabel(String msg, String description)
	{
		label = new Label(msg, description);
	}

	public void setDescription(String msg, String desc)
	{
		description = new Description(msg, desc);
	}

	public Description getDescription()
	{
		return description;
	}

	public Label getLabel()
	{
		return label;
	}

	public boolean isUserConfigurable()
	{
		return userConfigurable;
	}

	public void setUserConfigurable(boolean userConfigurable)
	{
		this.userConfigurable = userConfigurable;
	}

	public boolean isPassword()
	{
		return password;
	}

	public boolean getPassword()
	{
		return this.password;
	}

	public void setPassword(boolean password)
	{
		this.password = password;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getQualifiers()
	{
		return qualifiers;
	}

	public void setQualifiers(String qualifiers)
	{
		this.qualifiers = qualifiers;
	}

	public void clone(OVFProperty property, String mainKey)
	{
		this.key = mainKey;

		if (property.getDescription() != null)
		{
			this.description = new Description("", "");
			this.description.clone(property.getDescription());
		}

		if (property.getLabel() != null)
		{
			this.label = new Label("", "");
			this.label.clone(property.getLabel());
		}

		this.value = property.getValue();
		this.userConfigurable = property.isUserConfigurable();
		this.password = property.getPassword();
		this.type = property.getType();
		this.qualifiers = property.getQualifiers();
	}

	public class Description
	{
		private String msgid;
		private String description;

		public Description(String msgid, String description)
		{
			this.msgid = msgid;
			this.description = description;
		}

		public String getMsgid()
		{
			return msgid;
		}

		public String getDescription()
		{
			return description;
		}

		public void clone(Description desc)
		{
			this.description = desc.getDescription();
			this.msgid = desc.getMsgid();
		}
	}

	public class Label
	{
		private String msgid;
		private String description;

		public Label(String msgid, String description)
		{
			this.msgid = msgid;
			this.description = description;
		}

		public String getMsgid()
		{
			return msgid;
		}

		public String getDescription()
		{
			return description;
		}

		public void clone(Label label)
		{
			this.description = label.getDescription();
			this.msgid = label.getMsgid();
		}

	}
	//TODO StAX parser section start
	public OVFProperty(){}
	public OVFProperty(XMLEvent e,XMLEventReader r,HashMap<String, OVFProperty> vars, HashMap<String, OVFVirtualNetwork> net, ApplianceDescriptor app) throws XMLStreamException{
		boolean end=false;
		while(!end&&r.hasNext()){
			e=r.nextEvent();
			end=e.isEndElement()&&e.asEndElement().getName().getLocalPart().contentEquals("Property");
			if(!end){
				switch(e.getEventType()){
					case XMLEvent.START_ELEMENT:
						e.asStartElement();
				}
			}
		}
	}
	//TODO StAX parser section end

}
