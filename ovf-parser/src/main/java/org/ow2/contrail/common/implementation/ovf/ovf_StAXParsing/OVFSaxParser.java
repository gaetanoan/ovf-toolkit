//package org.ow2.contrail.common.implementation.ovf.ovf_StAXParsing;


//FIXME unused to complete or to remove.. 
//public class OVFSaxParser {
//
//	private ArrayList<String> stato;
//	private ApplicationDescriptor appDesc;
//	private Collection<ApplianceDescriptor> cAppD;
//	private Collection<ApplianceDescriptor> initial;
//	private ApplianceDescriptor father;
//	
//	public OVFSaxParser(){
//		stato=new ArrayList<String>();
//		appDesc=null;
//		cAppD=null;
//		initial=null;
//		father=null;
//	}
//	
//	public ApplicationDescriptor parseOVF(URI file) throws FileNotFoundException,XMLStreamException{
//		return parseOVF(new FileInputStream(file.getPath()),false);
//	}
//	
//	public ApplicationDescriptor parseOVF(String strbuf) throws UnsupportedEncodingException, XMLStreamException{
//		return parseOVF(new ByteArrayInputStream(strbuf.getBytes("UTF-8")),false);
//	}
//	
//	public ApplicationDescriptor parseOVF(InputStream str,boolean isValidating)throws XMLStreamException{
//		/*CLEAR OLD REFERENCES in MEMORY*/
//		stato.clear();appDesc=null;cAppD.clear();initial.clear();father=null;
//		XMLInputFactory factory = XMLInputFactory.newInstance();
//		factory.setProperty(XMLInputFactory.IS_VALIDATING, isValidating);
//		XMLEventReader r=factory.createFilteredReader(factory.createXMLEventReader(str),new FilterOVFElement());
//		
//		while(r.hasNext()){
//			XMLEvent e = r.nextEvent();
//			/*EVENT DISPATCHER*/
//			switch(e.getEventType()){
//				case XMLEvent.START_ELEMENT:
//					selectOperation(e.asStartElement().getName().getLocalPart(),e,r);break;
//				case XMLEvent.END_ELEMENT:
//					closingTagCleaning(e.asEndElement().getName().getLocalPart());break;
//			}
//		}
//		return appDesc;
//	}
//	
//	private void selectOperation(String tagName,XMLEvent e,XMLEventReader r){
//		if(tagName.contentEquals("Envelope"))createApplicationDescriptor(tagName,e);
//		else if(tagName.contentEquals("VirtualSystemCollection"))createApplianceDescriptor(tagName,true,e,r);
//		else if(tagName.contentEquals("VirtualSystem"))createApplianceDescriptor(tagName,false,e,r);
//		else if(tagName.contentEquals("ProductSection"));
//		else if(tagName.contentEquals("StartupSection")); 
//		else if(tagName.contentEquals("VirtualHardwareSection"));
//	}
//	
//	private void createApplianceDescriptor(String id,boolean isVirtualCollection,XMLEvent e,XMLEventReader r){
//		stato.add(isVirtualCollection?"VirtualSystemCollection":"VirtualSystem");
//		Attribute curs=e.asStartElement().getAttributeByName(QName.valueOf("{http://schemas.dmtf.org/ovf/envelope/1}id"));
//		ApplianceDescriptor  son= new ApplianceDescriptor(curs.getValue().trim(),null,0,isVirtualCollection);
//		son.setParent(father);
//		cAppD.add(son);
//		if(isVirtualCollection){
//			cAppD=son.getAppliancesDescriptors();
//			father=son;
//			
//		}
//	}
//	
//	private void createApplicationDescriptor(String id,XMLEvent e){
//		stato.add(0,"Envelope");
//		appDesc=new ApplicationDescriptor();
//		initial=cAppD=appDesc.getApplianceDescriptors();
//	}
//	
//	private void closingTagCleaning(String id){
//		if(isChangingStatus(id))stato.remove(0);
//		if(id.contentEquals("VirtualSystemCollection")){
//			father=father.getParent();
//			if(father!=null)cAppD=father.getAppliancesDescriptors();
//			else cAppD=initial;
//		}
//	}
//	
//	
//	public static void main(String[] args) throws FileNotFoundException, XMLStreamException{
//		String filetoParse="/home/giacomo/documenti/another/ovf-parser/src/main/resources/contrail_petstore.xml";
//		recursiveTest("",(new OVFSaxParser().parseOVF(URI.create(filetoParse))).getApplianceDescriptors());
//	}
//	
//	
//	private static boolean isChangingStatus(String str){
//		return str.contentEquals("VirtualSystemCollection")|| str.contentEquals("VirtualSystem")||
//				str.contentEquals("Envelope");
//	}
//	
//	private static void recursiveTest(String spaces, Collection<ApplianceDescriptor> cad){
//			for(ApplianceDescriptor a:cad){
//				System.out.println(spaces+a.getID());
//				recursiveTest(spaces+"\t",a.getAppliancesDescriptors());
//			}
//		}
//}
