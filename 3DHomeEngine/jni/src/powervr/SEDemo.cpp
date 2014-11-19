#include "PVRShell.h"
#include "SE_DynamicLibType.h"
#include <stdio.h>
#include <math.h>
#include "SE_Animation.h"
#include "SE_Mesh.h"
#include "SE_Ase.h"

#include "SE_ResourceManager.h"
#include "SE_Spatial.h"
#include "SE_Log.h"
#include "SE_Struct.h"
#include "SE_Common.h"
#include "SE_Camera.h"
#include "SE_Application.h"
#include "SE_SystemCommand.h"
#include "SE_SystemCommandFactory.h"
#include "SE_InputEvent.h"
#include "SE_InputManager.h"
#include "SE_Primitive.h"
#include "SE_Geometry.h"
#include "SE_GeometryData.h"
#include "SE_SimObject.h"
#include "SE_MeshSimObject.h"
#include "SE_SceneManager.h"
#include "SE_ImageCodec.h"
#include "SE_CommonNode.h"
#include "SE_2DCommand.h"
#include "SE_Bone.h"
#include "SE_BipedAnimation.h"
#include "SE_SkinJointController.h"
#include "SE_BipedController.h"

#include "SE_AnimationManager.h"
#include "SE_TableManager.h"

#include <ctype.h>
#include <stdarg.h>
#include "SE_TextureCoordData.h"

#include "SE_UserCommand.h"

#include "SE_MotionEventController.h"
#include "SE_DataValueDefine.h"
#include "SE_MotionEventSimObjectController.h"
#include "SE_MotionEventCamera.h"
#include "SE_RenderManager.h"
#include "SE_RenderSystemCapabilities.h"

#include "SE_ID.h"
#include "SE_Animation.h"
#include "SE_AnimationListener.h"
#ifdef WIN32
#else
#include <stdint.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <string>
#include <vector>

#include "SE_Light.h"
#include "SE_Factory.h"
#include "SE_PluginsManager.h"
#include "SE_Plugin.h"
#include "SE_MemLeakDetector.h"
#include "SE_ObjectManager.h"
#include "SE_RenderTargetManager.h"
#include "SE_RenderTarget.h"

#include "ParticleSystem/SE_ParticleSystemManager.h"

#define TINYXML
#include "tinyxml.h"
#ifdef TINYXML
#ifdef TIXML_USE_STL
	#include <iostream>
	#include <sstream>
	using namespace std;
#else
	#include <stdio.h>
#endif

#if defined( WIN32 ) && defined( TUNE )
	#include <crtdbg.h>
	_CrtMemState startMemState;
	_CrtMemState endMemState;
#endif



bool XmlTest (const char* testString, const char* expected, const char* found, bool noEcho = false);
bool XmlTest( const char* testString, int expected, int found, bool noEcho = false );

static int gPass = 0;
static int gFail = 0;

bool XmlTest (const char* testString, const char* expected, const char* found, bool noEcho )
{
	bool pass = !strcmp( expected, found );
	if ( pass )
		printf ("[pass]");
	else
		printf ("[fail]");

	if ( noEcho )
		printf (" %s\n", testString);
	else
		printf (" %s [%s][%s]\n", testString, expected, found);

	if ( pass )
		++gPass;
	else
		++gFail;
	return pass;
}


bool XmlTest( const char* testString, int expected, int found, bool noEcho )
{
	bool pass = ( expected == found );
	if ( pass )
		printf ("[pass]");
	else
		printf ("[fail]");

	if ( noEcho )
		printf (" %s\n", testString);
	else
		printf (" %s [%d][%d]\n", testString, expected, found);

	if ( pass )
		++gPass;
	else
		++gFail;
	return pass;
}


void NullLineEndings( char* p )
{
	while( p && *p ) {
		if ( *p == '\n' || *p == '\r' ) {
			*p = 0;
			return;
		}
		++p;
	}
}

#define printf LOGI
int testtiny()
{
//
	// We start with the 'demoStart' todo list. Process it. And
	// should hopefully end up with the todo list as illustrated.
	//
	const char* demoStart =
		"<?xml version=\"1.0\"  standalone='no' >\n"
		"<!-- Our to do list data -->"
		"<ToDo>\n"
		"<!-- Do I need a secure PDA? -->\n"
		"<Item priority=\"1\" distance='close'> Go to the <bold>Toy store!</bold></Item>"
		"<Item priority=\"2\" distance='none'> Do bills   </Item>"
		"<Item priority=\"2\" distance='far &amp; back'> Look for Evil Dinosaurs! </Item>"
		"</ToDo>";
		
	{

	#ifdef TIXML_USE_STL
		//	What the todo list should look like after processing.
		// In stream (no formatting) representation.
		const char* demoEnd =
			"<?xml version=\"1.0\" standalone=\"no\" ?>"
			"<!-- Our to do list data -->"
			"<ToDo>"
			"<!-- Do I need a secure PDA? -->"
			"<Item priority=\"2\" distance=\"close\">Go to the"
			"<bold>Toy store!"
			"</bold>"
			"</Item>"
			"<Item priority=\"1\" distance=\"far\">Talk to:"
			"<Meeting where=\"School\">"
			"<Attendee name=\"Marple\" position=\"teacher\" />"
			"<Attendee name=\"Voel\" position=\"counselor\" />"
			"</Meeting>"
			"<Meeting where=\"Lunch\" />"
			"</Item>"
			"<Item priority=\"2\" distance=\"here\">Do bills"
			"</Item>"
			"</ToDo>";
	#endif

		// The example parses from the character string (above):
		#if defined( WIN32 ) && defined( TUNE )
		_CrtMemCheckpoint( &startMemState );
		#endif	

		{
			// Write to a file and read it back, to check file I/O.

			TiXmlDocument doc( "demotest.xml" );
			doc.Parse( demoStart );

			if ( doc.Error() )
			{
				printf( "Error in %s: %s\n", doc.Value(), doc.ErrorDesc() );
				exit( 1 );
			}
			doc.SaveFile();
		}

		TiXmlDocument doc( "demotest.xml" );
		bool loadOkay = doc.LoadFile();

		if ( !loadOkay )
		{
			printf( "Could not load test file 'demotest.xml'. Error='%s'. Exiting.\n", doc.ErrorDesc() );
			exit( 1 );
		}

		printf( "** Demo doc read from disk: ** \n\n" );
		printf( "** Printing via doc.Print **\n" );
		doc.Print( stdout );

		{
			printf( "** Printing via TiXmlPrinter **\n" );
			TiXmlPrinter printer;
			doc.Accept( &printer );
			fprintf( stdout, "%s", printer.CStr() );
		}
		#ifdef TIXML_USE_STL	
		{
			printf( "** Printing via operator<< **\n" );
			std::cout << doc;
		}
		#endif
		TiXmlNode* node = 0;
		TiXmlElement* todoElement = 0;
		TiXmlElement* itemElement = 0;


		// --------------------------------------------------------
		// An example of changing existing attributes, and removing
		// an element from the document.
		// --------------------------------------------------------

		// Get the "ToDo" element.
		// It is a child of the document, and can be selected by name.
		node = doc.FirstChild( "ToDo" );
		assert( node );
		todoElement = node->ToElement();
		assert( todoElement  );

		// Going to the toy store is now our second priority...
		// So set the "priority" attribute of the first item in the list.
		node = todoElement->FirstChildElement();	// This skips the "PDA" comment.
		assert( node );
		itemElement = node->ToElement();
		assert( itemElement  );
		itemElement->SetAttribute( "priority", 2 );

		// Change the distance to "doing bills" from
		// "none" to "here". It's the next sibling element.
		itemElement = itemElement->NextSiblingElement();
		assert( itemElement );
		itemElement->SetAttribute( "distance", "here" );

		// Remove the "Look for Evil Dinosaurs!" item.
		// It is 1 more sibling away. We ask the parent to remove
		// a particular child.
		itemElement = itemElement->NextSiblingElement();
		todoElement->RemoveChild( itemElement );

		itemElement = 0;

		// --------------------------------------------------------
		// What follows is an example of created elements and text
		// nodes and adding them to the document.
		// --------------------------------------------------------

		// Add some meetings.
		TiXmlElement item( "Item" );
		item.SetAttribute( "priority", "1" );
		item.SetAttribute( "distance", "far" );

		TiXmlText text( "Talk to:" );

		TiXmlElement meeting1( "Meeting" );
		meeting1.SetAttribute( "where", "School" );

		TiXmlElement meeting2( "Meeting" );
		meeting2.SetAttribute( "where", "Lunch" );

		TiXmlElement attendee1( "Attendee" );
		attendee1.SetAttribute( "name", "Marple" );
		attendee1.SetAttribute( "position", "teacher" );

		TiXmlElement attendee2( "Attendee" );
		attendee2.SetAttribute( "name", "Voel" );
		attendee2.SetAttribute( "position", "counselor" );

		// Assemble the nodes we've created:
		meeting1.InsertEndChild( attendee1 );
		meeting1.InsertEndChild( attendee2 );

		item.InsertEndChild( text );
		item.InsertEndChild( meeting1 );
		item.InsertEndChild( meeting2 );

		// And add the node to the existing list after the first child.
		node = todoElement->FirstChild( "Item" );
		assert( node );
		itemElement = node->ToElement();
		assert( itemElement );

		todoElement->InsertAfterChild( itemElement, item );

		printf( "\n** Demo doc processed: ** \n\n" );
		doc.Print( stdout );


	#ifdef TIXML_USE_STL
		printf( "** Demo doc processed to stream: ** \n\n" );
		cout << doc << endl << endl;
	#endif

		// --------------------------------------------------------
		// Different tests...do we have what we expect?
		// --------------------------------------------------------

		int count = 0;
		TiXmlElement*	element;

		//////////////////////////////////////////////////////

	#ifdef TIXML_USE_STL
		cout << "** Basic structure. **\n";
		ostringstream outputStream( ostringstream::out );
		outputStream << doc;
		XmlTest( "Output stream correct.",	string( demoEnd ).c_str(),
											outputStream.str().c_str(), true );
	#endif

		node = doc.RootElement();
		assert( node );
		XmlTest( "Root element exists.", true, ( node != 0 && node->ToElement() ) );
		XmlTest ( "Root element value is 'ToDo'.", "ToDo",  node->Value());

		node = node->FirstChild();
		XmlTest( "First child exists & is a comment.", true, ( node != 0 && node->ToComment() ) );
		node = node->NextSibling();
		XmlTest( "Sibling element exists & is an element.", true, ( node != 0 && node->ToElement() ) );
		XmlTest ( "Value is 'Item'.", "Item", node->Value() );

		node = node->FirstChild();
		XmlTest ( "First child exists.", true, ( node != 0 && node->ToText() ) );
		XmlTest ( "Value is 'Go to the'.", "Go to the", node->Value() );


		//////////////////////////////////////////////////////
		printf ("\n** Iterators. **\n");

		// Walk all the top level nodes of the document.
		count = 0;
		for( node = doc.FirstChild();
			 node;
			 node = node->NextSibling() )
		{
			count++;
		}
		XmlTest( "Top level nodes, using First / Next.", 3, count );

		count = 0;
		for( node = doc.LastChild();
			 node;
			 node = node->PreviousSibling() )
		{
			count++;
		}
		XmlTest( "Top level nodes, using Last / Previous.", 3, count );

		// Walk all the top level nodes of the document,
		// using a different syntax.
		count = 0;
		for( node = doc.IterateChildren( 0 );
			 node;
			 node = doc.IterateChildren( node ) )
		{
			count++;
		}
		XmlTest( "Top level nodes, using IterateChildren.", 3, count );

		// Walk all the elements in a node.
		count = 0;
		for( element = todoElement->FirstChildElement();
			 element;
			 element = element->NextSiblingElement() )
		{
			count++;
		}
		XmlTest( "Children of the 'ToDo' element, using First / Next.",
			3, count );

		// Walk all the elements in a node by value.
		count = 0;
		for( node = todoElement->FirstChild( "Item" );
			 node;
			 node = node->NextSibling( "Item" ) )
		{
			count++;
		}
		XmlTest( "'Item' children of the 'ToDo' element, using First/Next.", 3, count );

		count = 0;
		for( node = todoElement->LastChild( "Item" );
			 node;
			 node = node->PreviousSibling( "Item" ) )
		{
			count++;
		}
		XmlTest( "'Item' children of the 'ToDo' element, using Last/Previous.", 3, count );

	#ifdef TIXML_USE_STL
		{
			cout << "\n** Parsing. **\n";
			istringstream parse0( "<Element0 attribute0='foo0' attribute1= noquotes attribute2 = '&gt;' />" );
			TiXmlElement element0( "default" );
			parse0 >> element0;

			XmlTest ( "Element parsed, value is 'Element0'.", "Element0", element0.Value() );
			XmlTest ( "Reads attribute 'attribute0=\"foo0\"'.", "foo0", element0.Attribute( "attribute0" ));
			XmlTest ( "Reads incorrectly formatted 'attribute1=noquotes'.", "noquotes", element0.Attribute( "attribute1" ) );
			XmlTest ( "Read attribute with entity value '>'.", ">", element0.Attribute( "attribute2" ) );
		}
	#endif

		{
			const char* error =	"<?xml version=\"1.0\" standalone=\"no\" ?>\n"
								"<passages count=\"006\" formatversion=\"20020620\">\n"
								"    <wrong error>\n"
								"</passages>";

			TiXmlDocument docTest;
			docTest.Parse( error );
			XmlTest( "Error row", docTest.ErrorRow(), 3 );
			XmlTest( "Error column", docTest.ErrorCol(), 17 );
			//printf( "error=%d id='%s' row %d col%d\n", (int) doc.Error(), doc.ErrorDesc(), doc.ErrorRow()+1, doc.ErrorCol() + 1 );

		}

	#ifdef TIXML_USE_STL
		{
			//////////////////////////////////////////////////////
			cout << "\n** Streaming. **\n";

			// Round trip check: stream in, then stream back out to verify. The stream
			// out has already been checked, above. We use the output

			istringstream inputStringStream( outputStream.str() );
			TiXmlDocument document0;

			inputStringStream >> document0;

			ostringstream outputStream0( ostringstream::out );
			outputStream0 << document0;

			XmlTest( "Stream round trip correct.",	string( demoEnd ).c_str(), 
													outputStream0.str().c_str(), true );

			std::string str;
			str << document0;

			XmlTest( "String printing correct.", string( demoEnd ).c_str(), 
												 str.c_str(), true );
		}
	#endif
	}

	{
		const char* str = "<doc attr0='1' attr1='2.0' attr2='foo' />";

		TiXmlDocument doc;
		doc.Parse( str );

		TiXmlElement* ele = doc.FirstChildElement();

		int iVal, result;
		double dVal;

		result = ele->QueryDoubleAttribute( "attr0", &dVal );
		XmlTest( "Query attribute: int as double", result, TIXML_SUCCESS );
		XmlTest( "Query attribute: int as double", (int)dVal, 1 );
		result = ele->QueryDoubleAttribute( "attr1", &dVal );
		XmlTest( "Query attribute: double as double", (int)dVal, 2 );
		result = ele->QueryIntAttribute( "attr1", &iVal );
		XmlTest( "Query attribute: double as int", result, TIXML_SUCCESS );
		XmlTest( "Query attribute: double as int", iVal, 2 );
		result = ele->QueryIntAttribute( "attr2", &iVal );
		XmlTest( "Query attribute: not a number", result, TIXML_WRONG_TYPE );
		result = ele->QueryIntAttribute( "bar", &iVal );
		XmlTest( "Query attribute: does not exist", result, TIXML_NO_ATTRIBUTE );
	}

	{
		const char* str = "<doc/>";

		TiXmlDocument doc;
		doc.Parse( str );

		TiXmlElement* ele = doc.FirstChildElement();

		int iVal;
		double dVal;

		ele->SetAttribute( "str", "strValue" );
		ele->SetAttribute( "int", 1 );
		ele->SetDoubleAttribute( "double", -1.0 );

		const char* cStr = ele->Attribute( "str" );
		ele->QueryIntAttribute( "int", &iVal );
		ele->QueryDoubleAttribute( "double", &dVal );

		XmlTest( "Attribute round trip. c-string.", "strValue", cStr );
		XmlTest( "Attribute round trip. int.", 1, iVal );
		XmlTest( "Attribute round trip. double.", -1, (int)dVal );
	}
	
	{
		const char* str =	"\t<?xml version=\"1.0\" standalone=\"no\" ?>\t<room doors='2'>\n"
							"</room>";

		TiXmlDocument doc;
		doc.SetTabSize( 8 );
		doc.Parse( str );

		TiXmlHandle docHandle( &doc );
		TiXmlHandle roomHandle = docHandle.FirstChildElement( "room" );

		assert( docHandle.Node() );
		assert( roomHandle.Element() );

		TiXmlElement* room = roomHandle.Element();
		assert( room );
		TiXmlAttribute* doors = room->FirstAttribute();
		assert( doors );

		XmlTest( "Location tracking: Tab 8: room row", room->Row(), 1 );
		XmlTest( "Location tracking: Tab 8: room col", room->Column(), 49 );
		XmlTest( "Location tracking: Tab 8: doors row", doors->Row(), 1 );
		XmlTest( "Location tracking: Tab 8: doors col", doors->Column(), 55 );
	}
	
	{
		const char* str =	"\t<?xml version=\"1.0\" standalone=\"no\" ?>\t<room doors='2'>\n"
							"  <!-- Silly example -->\n"
							"    <door wall='north'>A great door!</door>\n"
							"\t<door wall='east'/>"
							"</room>";

		TiXmlDocument doc;
		doc.Parse( str );

		TiXmlHandle docHandle( &doc );
		TiXmlHandle roomHandle = docHandle.FirstChildElement( "room" );
		TiXmlHandle commentHandle = docHandle.FirstChildElement( "room" ).FirstChild();
		TiXmlHandle textHandle = docHandle.FirstChildElement( "room" ).ChildElement( "door", 0 ).FirstChild();
		TiXmlHandle door0Handle = docHandle.FirstChildElement( "room" ).ChildElement( 0 );
		TiXmlHandle door1Handle = docHandle.FirstChildElement( "room" ).ChildElement( 1 );

		assert( docHandle.Node() );
		assert( roomHandle.Element() );
		assert( commentHandle.Node() );
		assert( textHandle.Text() );
		assert( door0Handle.Element() );
		assert( door1Handle.Element() );

		TiXmlDeclaration* declaration = doc.FirstChild()->ToDeclaration();
		assert( declaration );
		TiXmlElement* room = roomHandle.Element();
		assert( room );
		TiXmlAttribute* doors = room->FirstAttribute();
		assert( doors );
		TiXmlText* text = textHandle.Text();
		TiXmlComment* comment = commentHandle.Node()->ToComment();
		assert( comment );
		TiXmlElement* door0 = door0Handle.Element();
		TiXmlElement* door1 = door1Handle.Element();

		XmlTest( "Location tracking: Declaration row", declaration->Row(), 1 );
		XmlTest( "Location tracking: Declaration col", declaration->Column(), 5 );
		XmlTest( "Location tracking: room row", room->Row(), 1 );
		XmlTest( "Location tracking: room col", room->Column(), 45 );
		XmlTest( "Location tracking: doors row", doors->Row(), 1 );
		XmlTest( "Location tracking: doors col", doors->Column(), 51 );
		XmlTest( "Location tracking: Comment row", comment->Row(), 2 );
		XmlTest( "Location tracking: Comment col", comment->Column(), 3 );
		XmlTest( "Location tracking: text row", text->Row(), 3 ); 
		XmlTest( "Location tracking: text col", text->Column(), 24 );
		XmlTest( "Location tracking: door0 row", door0->Row(), 3 );
		XmlTest( "Location tracking: door0 col", door0->Column(), 5 );
		XmlTest( "Location tracking: door1 row", door1->Row(), 4 );
		XmlTest( "Location tracking: door1 col", door1->Column(), 5 );
	}


	// --------------------------------------------------------
	// UTF-8 testing. It is important to test:
	//	1. Making sure name, value, and text read correctly
	//	2. Row, Col functionality
	//	3. Correct output
	// --------------------------------------------------------
	printf ("\n** UTF-8 **\n");
	{
		TiXmlDocument doc( "utf8test.xml" );
		doc.LoadFile();
		if ( doc.Error() && doc.ErrorId() == TiXmlBase::TIXML_ERROR_OPENING_FILE ) {
			printf( "WARNING: File 'utf8test.xml' not found.\n"
					"(Are you running the test from the wrong directory?)\n"
				    "Could not test UTF-8 functionality.\n" );
		}
		else
		{
			TiXmlHandle docH( &doc );
			// Get the attribute "value" from the "Russian" element and check it.
			TiXmlElement* element = docH.FirstChildElement( "document" ).FirstChildElement( "Russian" ).Element();
			const unsigned char correctValue[] = {	0xd1U, 0x86U, 0xd0U, 0xb5U, 0xd0U, 0xbdU, 0xd0U, 0xbdU, 
													0xd0U, 0xbeU, 0xd1U, 0x81U, 0xd1U, 0x82U, 0xd1U, 0x8cU, 0 };

			XmlTest( "UTF-8: Russian value.", (const char*)correctValue, element->Attribute( "value" ), true );
			XmlTest( "UTF-8: Russian value row.", 4, element->Row() );
			XmlTest( "UTF-8: Russian value column.", 5, element->Column() );

			const unsigned char russianElementName[] = {	0xd0U, 0xa0U, 0xd1U, 0x83U,
															0xd1U, 0x81U, 0xd1U, 0x81U,
															0xd0U, 0xbaU, 0xd0U, 0xb8U,
															0xd0U, 0xb9U, 0 };
			const char russianText[] = "<\xD0\xB8\xD0\xBC\xD0\xB5\xD0\xB5\xD1\x82>";

			TiXmlText* text = docH.FirstChildElement( "document" ).FirstChildElement( (const char*) russianElementName ).Child( 0 ).Text();
			XmlTest( "UTF-8: Browsing russian element name.",
					 russianText,
					 text->Value(),
					 true );
			XmlTest( "UTF-8: Russian element name row.", 7, text->Row() );
			XmlTest( "UTF-8: Russian element name column.", 47, text->Column() );

			TiXmlDeclaration* dec = docH.Child( 0 ).Node()->ToDeclaration();
			XmlTest( "UTF-8: Declaration column.", 1, dec->Column() );
			XmlTest( "UTF-8: Document column.", 1, doc.Column() );

			// Now try for a round trip.
			doc.SaveFile( "utf8testout.xml" );

			// Check the round trip.
			char savedBuf[256];
			char verifyBuf[256];
			int okay = 1;

			FILE* saved  = fopen( "utf8testout.xml", "r" );
			FILE* verify = fopen( "utf8testverify.xml", "r" );

			//bool firstLineBOM=true;
			if ( saved && verify )
			{
				while ( fgets( verifyBuf, 256, verify ) )
				{
					fgets( savedBuf, 256, saved );
					NullLineEndings( verifyBuf );
					NullLineEndings( savedBuf );

					if ( /*!firstLineBOM && */ strcmp( verifyBuf, savedBuf ) )
					{
						printf( "verify:%s<\n", verifyBuf );
						printf( "saved :%s<\n", savedBuf );
						okay = 0;
						break;
					}
					//firstLineBOM = false;
				}
			}
			if ( saved )
				fclose( saved );
			if ( verify )
				fclose( verify );
			XmlTest( "UTF-8: Verified multi-language round trip.", 1, okay );

			// On most Western machines, this is an element that contains
			// the word "resume" with the correct accents, in a latin encoding.
			// It will be something else completely on non-wester machines,
			// which is why TinyXml is switching to UTF-8.
			const char latin[] = "<element>r\x82sum\x82</element>";

			TiXmlDocument latinDoc;
			latinDoc.Parse( latin, 0, TIXML_ENCODING_LEGACY );

			text = latinDoc.FirstChildElement()->FirstChild()->ToText();
			XmlTest( "Legacy encoding: Verify text element.", "r\x82sum\x82", text->Value() );
		}
	}		

	//////////////////////
	// Copy and assignment
	//////////////////////
	printf ("\n** Copy and Assignment **\n");
	{
		TiXmlElement element( "foo" );
		element.Parse( "<element name='value' />", 0, TIXML_ENCODING_UNKNOWN );

		TiXmlElement elementCopy( element );
		TiXmlElement elementAssign( "foo" );
		elementAssign.Parse( "<incorrect foo='bar'/>", 0, TIXML_ENCODING_UNKNOWN );
		elementAssign = element;

		XmlTest( "Copy/Assign: element copy #1.", "element", elementCopy.Value() );
		XmlTest( "Copy/Assign: element copy #2.", "value", elementCopy.Attribute( "name" ) );
		XmlTest( "Copy/Assign: element assign #1.", "element", elementAssign.Value() );
		XmlTest( "Copy/Assign: element assign #2.", "value", elementAssign.Attribute( "name" ) );
		XmlTest( "Copy/Assign: element assign #3.", true, ( 0 == elementAssign.Attribute( "foo" )) );

		TiXmlComment comment;
		comment.Parse( "<!--comment-->", 0, TIXML_ENCODING_UNKNOWN );
		TiXmlComment commentCopy( comment );
		TiXmlComment commentAssign;
		commentAssign = commentCopy;
		XmlTest( "Copy/Assign: comment copy.", "comment", commentCopy.Value() );
		XmlTest( "Copy/Assign: comment assign.", "comment", commentAssign.Value() );

		TiXmlUnknown unknown;
		unknown.Parse( "<[unknown]>", 0, TIXML_ENCODING_UNKNOWN );
		TiXmlUnknown unknownCopy( unknown );
		TiXmlUnknown unknownAssign;
		unknownAssign.Parse( "incorrect", 0, TIXML_ENCODING_UNKNOWN );
		unknownAssign = unknownCopy;
		XmlTest( "Copy/Assign: unknown copy.", "[unknown]", unknownCopy.Value() );
		XmlTest( "Copy/Assign: unknown assign.", "[unknown]", unknownAssign.Value() );
		
		TiXmlText text( "TextNode" );
		TiXmlText textCopy( text );
		TiXmlText textAssign( "incorrect" );
		textAssign = text;
		XmlTest( "Copy/Assign: text copy.", "TextNode", textCopy.Value() );
		XmlTest( "Copy/Assign: text assign.", "TextNode", textAssign.Value() );

		TiXmlDeclaration dec;
		dec.Parse( "<?xml version='1.0' encoding='UTF-8'?>", 0, TIXML_ENCODING_UNKNOWN );
		TiXmlDeclaration decCopy( dec );
		TiXmlDeclaration decAssign;
		decAssign = dec;

		XmlTest( "Copy/Assign: declaration copy.", "UTF-8", decCopy.Encoding() );
		XmlTest( "Copy/Assign: text assign.", "UTF-8", decAssign.Encoding() );

		TiXmlDocument doc;
		elementCopy.InsertEndChild( textCopy );
		doc.InsertEndChild( decAssign );
		doc.InsertEndChild( elementCopy );
		doc.InsertEndChild( unknownAssign );

		TiXmlDocument docCopy( doc );
		TiXmlDocument docAssign;
		docAssign = docCopy;

		#ifdef TIXML_USE_STL
		std::string original, copy, assign;
		original << doc;
		copy << docCopy;
		assign << docAssign;
		XmlTest( "Copy/Assign: document copy.", original.c_str(), copy.c_str(), true );
		XmlTest( "Copy/Assign: document assign.", original.c_str(), assign.c_str(), true );

		#endif
	}	

	//////////////////////////////////////////////////////
#ifdef TIXML_USE_STL
	printf ("\n** Parsing, no Condense Whitespace **\n");
	TiXmlBase::SetCondenseWhiteSpace( false );
	{
		istringstream parse1( "<start>This  is    \ntext</start>" );
		TiXmlElement text1( "text" );
		parse1 >> text1;

		XmlTest ( "Condense white space OFF.", "This  is    \ntext",
					text1.FirstChild()->Value(),
					true );
	}
	TiXmlBase::SetCondenseWhiteSpace( true );
#endif

	//////////////////////////////////////////////////////
	// GetText();
	{
		const char* str = "<foo>This is text</foo>";
		TiXmlDocument doc;
		doc.Parse( str );
		const TiXmlElement* element = doc.RootElement();

		XmlTest( "GetText() normal use.", "This is text", element->GetText() );

		str = "<foo><b>This is text</b></foo>";
		doc.Clear();
		doc.Parse( str );
		element = doc.RootElement();

		XmlTest( "GetText() contained element.", element->GetText() == 0, true );

		str = "<foo>This is <b>text</b></foo>";
		doc.Clear();
		TiXmlBase::SetCondenseWhiteSpace( false );
		doc.Parse( str );
		TiXmlBase::SetCondenseWhiteSpace( true );
		element = doc.RootElement();

		XmlTest( "GetText() partial.", "This is ", element->GetText() );
	}


	//////////////////////////////////////////////////////
	// CDATA
	{
		const char* str =	"<xmlElement>"
								"<![CDATA["
									"I am > the rules!\n"
									"...since I make symbolic puns"
								"]]>"
							"</xmlElement>";
		TiXmlDocument doc;
		doc.Parse( str );
		doc.Print();

		XmlTest( "CDATA parse.", doc.FirstChildElement()->FirstChild()->Value(), 
								 "I am > the rules!\n...since I make symbolic puns",
								 true );

		#ifdef TIXML_USE_STL
		//cout << doc << '\n';

		doc.Clear();

		istringstream parse0( str );
		parse0 >> doc;
		//cout << doc << '\n';

		XmlTest( "CDATA stream.", doc.FirstChildElement()->FirstChild()->Value(), 
								 "I am > the rules!\n...since I make symbolic puns",
								 true );
		#endif

		TiXmlDocument doc1 = doc;
		//doc.Print();

		XmlTest( "CDATA copy.", doc1.FirstChildElement()->FirstChild()->Value(), 
								 "I am > the rules!\n...since I make symbolic puns",
								 true );
	}
	{
		// [ 1482728 ] Wrong wide char parsing
		char buf[256];
		buf[255] = 0;
		for( int i=0; i<255; ++i ) {
			buf[i] = (char)((i>=32) ? i : 32);
		}
		TIXML_STRING str( "<xmlElement><![CDATA[" );
		str += buf;
		str += "]]></xmlElement>";

		TiXmlDocument doc;
		doc.Parse( str.c_str() );

		TiXmlPrinter printer;
		printer.SetStreamPrinting();
		doc.Accept( &printer );

		XmlTest( "CDATA with all bytes #1.", str.c_str(), printer.CStr(), true );

		#ifdef TIXML_USE_STL
		doc.Clear();
		istringstream iss( printer.Str() );
		iss >> doc;
		std::string out;
		out << doc;
		XmlTest( "CDATA with all bytes #2.", out.c_str(), printer.CStr(), true );
		#endif
	}
	{
		// [ 1480107 ] Bug-fix for STL-streaming of CDATA that contains tags
		// CDATA streaming had a couple of bugs, that this tests for.
		const char* str =	"<xmlElement>"
								"<![CDATA["
									"<b>I am > the rules!</b>\n"
									"...since I make symbolic puns"
								"]]>"
							"</xmlElement>";
		TiXmlDocument doc;
		doc.Parse( str );
		doc.Print();

		XmlTest( "CDATA parse. [ 1480107 ]", doc.FirstChildElement()->FirstChild()->Value(), 
								 "<b>I am > the rules!</b>\n...since I make symbolic puns",
								 true );

		#ifdef TIXML_USE_STL

		doc.Clear();

		istringstream parse0( str );
		parse0 >> doc;

		XmlTest( "CDATA stream. [ 1480107 ]", doc.FirstChildElement()->FirstChild()->Value(), 
								 "<b>I am > the rules!</b>\n...since I make symbolic puns",
								 true );
		#endif

		TiXmlDocument doc1 = doc;
		//doc.Print();

		XmlTest( "CDATA copy. [ 1480107 ]", doc1.FirstChildElement()->FirstChild()->Value(), 
								 "<b>I am > the rules!</b>\n...since I make symbolic puns",
								 true );
	}
	//////////////////////////////////////////////////////
	// Visit()



	//////////////////////////////////////////////////////
	printf( "\n** Fuzzing... **\n" );

	const int FUZZ_ITERATION = 300;

	// The only goal is not to crash on bad input.
	int len = (int) strlen( demoStart );
	for( int i=0; i<FUZZ_ITERATION; ++i ) 
	{
		char* demoCopy = new char[ len+1 ];
		strcpy( demoCopy, demoStart );

		demoCopy[ i%len ] = (char)((i+1)*3);
		demoCopy[ (i*7)%len ] = '>';
		demoCopy[ (i*11)%len ] = '<';

		TiXmlDocument xml;
		xml.Parse( demoCopy );

		delete [] demoCopy;
	}
	printf( "** Fuzzing Complete. **\n" );
	
	//////////////////////////////////////////////////////
	printf ("\n** Bug regression tests **\n");

	// InsertBeforeChild and InsertAfterChild causes crash.
	{
		TiXmlElement parent( "Parent" );
		TiXmlElement childText0( "childText0" );
		TiXmlElement childText1( "childText1" );
		TiXmlNode* childNode0 = parent.InsertEndChild( childText0 );
		TiXmlNode* childNode1 = parent.InsertBeforeChild( childNode0, childText1 );

		XmlTest( "Test InsertBeforeChild on empty node.", ( childNode1 == parent.FirstChild() ), true );
	}

	{
		// InsertBeforeChild and InsertAfterChild causes crash.
		TiXmlElement parent( "Parent" );
		TiXmlElement childText0( "childText0" );
		TiXmlElement childText1( "childText1" );
		TiXmlNode* childNode0 = parent.InsertEndChild( childText0 );
		TiXmlNode* childNode1 = parent.InsertAfterChild( childNode0, childText1 );

		XmlTest( "Test InsertAfterChild on empty node. ", ( childNode1 == parent.LastChild() ), true );
	}

	// Reports of missing constructors, irregular string problems.
	{
		// Missing constructor implementation. No test -- just compiles.
		TiXmlText text( "Missing" );

		#ifdef TIXML_USE_STL
			// Missing implementation:
			TiXmlDocument doc;
			string name = "missing";
			doc.LoadFile( name );

			TiXmlText textSTL( name );
		#else
			// verifying some basic string functions:
			TiXmlString a;
			TiXmlString b( "Hello" );
			TiXmlString c( "ooga" );

			c = " World!";
			a = b;
			a += c;
			a = a;

			XmlTest( "Basic TiXmlString test. ", "Hello World!", a.c_str() );
		#endif
 	}

	// Long filenames crashing STL version
	{
		TiXmlDocument doc( "midsummerNightsDreamWithAVeryLongFilenameToConfuseTheStringHandlingRoutines.xml" );
		bool loadOkay = doc.LoadFile();
		loadOkay = true;	// get rid of compiler warning.
		// Won't pass on non-dev systems. Just a "no crash" check.
		//XmlTest( "Long filename. ", true, loadOkay );
	}

	{
		// Entities not being written correctly.
		// From Lynn Allen

		const char* passages =
			"<?xml version=\"1.0\" standalone=\"no\" ?>"
			"<passages count=\"006\" formatversion=\"20020620\">"
				"<psg context=\"Line 5 has &quot;quotation marks&quot; and &apos;apostrophe marks&apos;."
				" It also has &lt;, &gt;, and &amp;, as well as a fake copyright &#xA9;.\"> </psg>"
			"</passages>";

		TiXmlDocument doc( "passages.xml" );
		doc.Parse( passages );
		TiXmlElement* psg = doc.RootElement()->FirstChildElement();
		const char* context = psg->Attribute( "context" );
		const char* expected = "Line 5 has \"quotation marks\" and 'apostrophe marks'. It also has <, >, and &, as well as a fake copyright \xC2\xA9.";

		XmlTest( "Entity transformation: read. ", expected, context, true );

		FILE* textfile = fopen( "textfile.txt", "w" );
		if ( textfile )
		{
			psg->Print( textfile, 0 );
			fclose( textfile );
		}
		textfile = fopen( "textfile.txt", "r" );
		assert( textfile );
		if ( textfile )
		{
			char buf[ 1024 ];
			fgets( buf, 1024, textfile );
			XmlTest( "Entity transformation: write. ",
					 "<psg context=\'Line 5 has &quot;quotation marks&quot; and &apos;apostrophe marks&apos;."
					 " It also has &lt;, &gt;, and &amp;, as well as a fake copyright \xC2\xA9.' />",
					 buf,
					 true );
		}
		fclose( textfile );
	}

    {
		FILE* textfile = fopen( "test5.xml", "w" );
		if ( textfile )
		{
            fputs("<?xml version='1.0'?><a.elem xmi.version='2.0'/>", textfile);
            fclose(textfile);

			TiXmlDocument doc;
            doc.LoadFile( "test5.xml" );
            XmlTest( "dot in element attributes and names", doc.Error(), 0);
		}
    }

	{
		FILE* textfile = fopen( "test6.xml", "w" );
		if ( textfile )
		{
            fputs("<element><Name>1.1 Start easy ignore fin thickness&#xA;</Name></element>", textfile );
            fclose(textfile);

            TiXmlDocument doc;
            bool result = doc.LoadFile( "test6.xml" );
            XmlTest( "Entity with one digit.", result, true );

			TiXmlText* text = doc.FirstChildElement()->FirstChildElement()->FirstChild()->ToText();
			XmlTest( "Entity with one digit.",
						text->Value(), "1.1 Start easy ignore fin thickness\n" );
		}
    }

	{
		// DOCTYPE not preserved (950171)
		// 
		const char* doctype =
			"<?xml version=\"1.0\" ?>"
			"<!DOCTYPE PLAY SYSTEM 'play.dtd'>"
			"<!ELEMENT title (#PCDATA)>"
			"<!ELEMENT books (title,authors)>"
			"<element />";

		TiXmlDocument doc;
		doc.Parse( doctype );
		doc.SaveFile( "test7.xml" );
		doc.Clear();
		doc.LoadFile( "test7.xml" );
		
		TiXmlHandle docH( &doc );
		TiXmlUnknown* unknown = docH.Child( 1 ).Unknown();
		XmlTest( "Correct value of unknown.", "!DOCTYPE PLAY SYSTEM 'play.dtd'", unknown->Value() );
		#ifdef TIXML_USE_STL
		TiXmlNode* node = docH.Child( 2 ).Node();
		std::string str;
		str << (*node);
		XmlTest( "Correct streaming of unknown.", "<!ELEMENT title (#PCDATA)>", str.c_str() );
		#endif
	}

	{
		// [ 791411 ] Formatting bug
		// Comments do not stream out correctly.
		const char* doctype = 
			"<!-- Somewhat<evil> -->";
		TiXmlDocument doc;
		doc.Parse( doctype );

		TiXmlHandle docH( &doc );
		TiXmlComment* comment = docH.Child( 0 ).Node()->ToComment();

		XmlTest( "Comment formatting.", " Somewhat<evil> ", comment->Value() );
		#ifdef TIXML_USE_STL
		std::string str;
		str << (*comment);
		XmlTest( "Comment streaming.", "<!-- Somewhat<evil> -->", str.c_str() );
		#endif
	}

	{
		// [ 870502 ] White space issues
		TiXmlDocument doc;
		TiXmlText* text;
		TiXmlHandle docH( &doc );
	
		const char* doctype0 = "<element> This has leading and trailing space </element>";
		const char* doctype1 = "<element>This has  internal space</element>";
		const char* doctype2 = "<element> This has leading, trailing, and  internal space </element>";

		TiXmlBase::SetCondenseWhiteSpace( false );
		doc.Clear();
		doc.Parse( doctype0 );
		text = docH.FirstChildElement( "element" ).Child( 0 ).Text();
		XmlTest( "White space kept.", " This has leading and trailing space ", text->Value() );

		doc.Clear();
		doc.Parse( doctype1 );
		text = docH.FirstChildElement( "element" ).Child( 0 ).Text();
		XmlTest( "White space kept.", "This has  internal space", text->Value() );

		doc.Clear();
		doc.Parse( doctype2 );
		text = docH.FirstChildElement( "element" ).Child( 0 ).Text();
		XmlTest( "White space kept.", " This has leading, trailing, and  internal space ", text->Value() );

		TiXmlBase::SetCondenseWhiteSpace( true );
		doc.Clear();
		doc.Parse( doctype0 );
		text = docH.FirstChildElement( "element" ).Child( 0 ).Text();
		XmlTest( "White space condensed.", "This has leading and trailing space", text->Value() );

		doc.Clear();
		doc.Parse( doctype1 );
		text = docH.FirstChildElement( "element" ).Child( 0 ).Text();
		XmlTest( "White space condensed.", "This has internal space", text->Value() );

		doc.Clear();
		doc.Parse( doctype2 );
		text = docH.FirstChildElement( "element" ).Child( 0 ).Text();
		XmlTest( "White space condensed.", "This has leading, trailing, and internal space", text->Value() );
	}

	{
		// Double attributes
		const char* doctype = "<element attr='red' attr='blue' />";

		TiXmlDocument doc;
		doc.Parse( doctype );
		
		XmlTest( "Parsing repeated attributes.", true, doc.Error() );	// is an  error to tinyxml (didn't use to be, but caused issues)
		//XmlTest( "Parsing repeated attributes.", "blue", doc.FirstChildElement( "element" )->Attribute( "attr" ) );
	}

	{
		// Embedded null in stream.
		const char* doctype = "<element att\0r='red' attr='blue' />";

		TiXmlDocument doc;
		doc.Parse( doctype );
		XmlTest( "Embedded null throws error.", true, doc.Error() );

		#ifdef TIXML_USE_STL
		istringstream strm( doctype );
		doc.Clear();
		doc.ClearError();
		strm >> doc;
		XmlTest( "Embedded null throws error.", true, doc.Error() );
		#endif
	}

#if 0
    {
            // Legacy mode test. (This test may only pass on a western system)
            const char* str =
                        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
                        "<?"
                        "fail test"
                        "</?";

            TiXmlDocument doc;
            doc.Parse( str );

            TiXmlHandle docHandle( &doc );
            TiXmlHandle aHandle = docHandle.FirstChildElement( "? );
            TiXmlHandle tHandle = aHandle.Child( 0 );
            assert( aHandle.Element() );
            assert( tHandle.Text() );
            XmlTest( "ISO-8859-1 Parsing.", "CöntäntßäöüÄÖ?, tHandle.Text()->Value() );
    }
#endif

	{
		// Empty documents should return TIXML_ERROR_PARSING_EMPTY, bug 1070717
		const char* str = "    ";
		TiXmlDocument doc;
		doc.Parse( str );
		XmlTest( "Empty document error TIXML_ERROR_DOCUMENT_EMPTY", TiXmlBase::TIXML_ERROR_DOCUMENT_EMPTY, doc.ErrorId() );
	}
	#ifndef TIXML_USE_STL
	{
		// String equality. [ 1006409 ] string operator==/!= no worky in all cases
		TiXmlString temp;
		XmlTest( "Empty tinyxml string compare equal", ( temp == "" ), true );

		TiXmlString    foo;
		TiXmlString    bar( "" );
		XmlTest( "Empty tinyxml string compare equal", ( foo == bar ), true );
	}

	#endif
	{
		// Bug [ 1195696 ] from marlonism
		TiXmlBase::SetCondenseWhiteSpace(false); 
		TiXmlDocument xml; 
		xml.Parse("<text><break/>This hangs</text>"); 
		XmlTest( "Test safe error return.", xml.Error(), false );
	}

	{
		// Bug [ 1243992 ] - another infinite loop
		TiXmlDocument doc;
		doc.SetCondenseWhiteSpace(false);
		doc.Parse("<p><pb></pb>test</p>");
	} 
	{
		// Low entities
		TiXmlDocument xml;
		xml.Parse( "<test>&#x0e;</test>" );
		const char result[] = { 0x0e, 0 };
		XmlTest( "Low entities.", xml.FirstChildElement()->GetText(), result );
		xml.Print();
	}
	{
		// Bug [ 1451649 ] Attribute values with trailing quotes not handled correctly
		TiXmlDocument xml;
		xml.Parse( "<foo attribute=bar\" />" );
		XmlTest( "Throw error with bad end quotes.", xml.Error(), true );
	}
	#ifdef TIXML_USE_STL
	{
		// Bug [ 1449463 ] Consider generic query
		TiXmlDocument xml;
		xml.Parse( "<foo bar='3' barStr='a string'/>" );

		TiXmlElement* ele = xml.FirstChildElement();
		double d;
		int i;
		float f;
		bool b;
		std::string str;

		XmlTest( "QueryValueAttribute", ele->QueryValueAttribute( "bar", &d ), TIXML_SUCCESS );
		XmlTest( "QueryValueAttribute", ele->QueryValueAttribute( "bar", &i ), TIXML_SUCCESS );
		XmlTest( "QueryValueAttribute", ele->QueryValueAttribute( "bar", &f ), TIXML_SUCCESS );
		XmlTest( "QueryValueAttribute", ele->QueryValueAttribute( "bar", &b ), TIXML_WRONG_TYPE );
		XmlTest( "QueryValueAttribute", ele->QueryValueAttribute( "nobar", &b ), TIXML_NO_ATTRIBUTE );
		XmlTest( "QueryValueAttribute", ele->QueryValueAttribute( "barStr", &str ), TIXML_SUCCESS );

		XmlTest( "QueryValueAttribute", (d==3.0), true );
		XmlTest( "QueryValueAttribute", (i==3), true );
		XmlTest( "QueryValueAttribute", (f==3.0f), true );
		XmlTest( "QueryValueAttribute", (str==std::string( "a string" )), true );
	}
	#endif

	#ifdef TIXML_USE_STL
	{
		// [ 1505267 ] redundant malloc in TiXmlElement::Attribute
		TiXmlDocument xml;
		xml.Parse( "<foo bar='3' />" );
		TiXmlElement* ele = xml.FirstChildElement();
		double d;
		int i;

		std::string bar = "bar";

		const std::string* atrrib = ele->Attribute( bar );
		ele->Attribute( bar, &d );
		ele->Attribute( bar, &i );

		XmlTest( "Attribute", atrrib->empty(), false );
		XmlTest( "Attribute", (d==3.0), true );
		XmlTest( "Attribute", (i==3), true );
	}
	#endif

	{
		// [ 1356059 ] Allow TiXMLDocument to only be at the top level
		TiXmlDocument xml, xml2;
		xml.InsertEndChild( xml2 );
		XmlTest( "Document only at top level.", xml.Error(), true );
		XmlTest( "Document only at top level.", xml.ErrorId(), TiXmlBase::TIXML_ERROR_DOCUMENT_TOP_ONLY );
	}

	{
		// [ 1663758 ] Failure to report error on bad XML
		TiXmlDocument xml;
		xml.Parse("<x>");
		XmlTest("Missing end tag at end of input", xml.Error(), true);
		xml.Parse("<x> ");
		XmlTest("Missing end tag with trailing whitespace", xml.Error(), true);
	} 

	{
		// [ 1635701 ] fail to parse files with a tag separated into two lines
		// I'm not sure this is a bug. Marked 'pending' for feedback.
		TiXmlDocument xml;
		xml.Parse( "<title><p>text</p\n><title>" );
		//xml.Print();
		//XmlTest( "Tag split by newline", xml.Error(), false );
	}

	#ifdef TIXML_USE_STL
	{
		// [ 1475201 ] TinyXML parses entities in comments
		TiXmlDocument xml;
		istringstream parse1( "<!-- declarations for <head> & <body> -->"
						      "<!-- far &amp; away -->" );
		parse1 >> xml;

		TiXmlNode* e0 = xml.FirstChild();
		TiXmlNode* e1 = e0->NextSibling();
		TiXmlComment* c0 = e0->ToComment();
		TiXmlComment* c1 = e1->ToComment();

		XmlTest( "Comments ignore entities.", " declarations for <head> & <body> ", c0->Value(), true );
		XmlTest( "Comments ignore entities.", " far &amp; away ", c1->Value(), true );
	}
	#endif

	{
		// [ 1475201 ] TinyXML parses entities in comments
		TiXmlDocument xml;
		xml.Parse("<!-- declarations for <head> & <body> -->"
				  "<!-- far &amp; away -->" );

		TiXmlNode* e0 = xml.FirstChild();
		TiXmlNode* e1 = e0->NextSibling();
		TiXmlComment* c0 = e0->ToComment();
		TiXmlComment* c1 = e1->ToComment();

		XmlTest( "Comments ignore entities.", " declarations for <head> & <body> ", c0->Value(), true );
		XmlTest( "Comments ignore entities.", " far &amp; away ", c1->Value(), true );
	}

	{
		TiXmlDocument xml;
		xml.Parse( "<Parent>"
						"<child1 att=''/>"
						"<!-- With this comment, child2 will not be parsed! -->"
						"<child2 att=''/>"
					"</Parent>" );
		int count = 0;

		TiXmlNode* ele = 0;
		while ( (ele = xml.FirstChildElement( "Parent" )->IterateChildren( ele ) ) != 0 ) {
			++count;
		}
		XmlTest( "Comments iterate correctly.", 3, count );
	}

	{
		// trying to repro ]1874301]. If it doesn't go into an infinite loop, all is well.
		unsigned char buf[] = "<?xml version=\"1.0\" encoding=\"utf-8\"?><feed><![CDATA[Test XMLblablablalblbl";
		buf[60] = 239;
		buf[61] = 0;

		TiXmlDocument doc;
		doc.Parse( (const char*)buf);
	} 


	{
		// bug 1827248 Error while parsing a little bit malformed file
		// Actually not malformed - should work.
		TiXmlDocument xml;
		xml.Parse( "<attributelist> </attributelist >" );
		XmlTest( "Handle end tag whitespace", false, xml.Error() );
	}

	{
		// This one must not result in an infinite loop
		TiXmlDocument xml;
		xml.Parse( "<infinite>loop" );
		XmlTest( "Infinite loop test.", true, true );
	}

	{
		// 1709904 - can not repro the crash
		{
			TiXmlDocument xml;
			xml.Parse( "<tag>/</tag>" );
			XmlTest( "Odd XML parsing.", xml.FirstChild()->Value(), "tag" );
		}
		/* Could not repro. {
			TiXmlDocument xml;
			xml.LoadFile( "EQUI_Inventory.xml" );
			//XmlTest( "Odd XML parsing.", xml.FirstChildElement()->Value(), "XML" );
			TiXmlPrinter printer;
			xml.Accept( &printer );
			fprintf( stdout, "%s", printer.CStr() );
		}*/
	}

	/*  1417717 experiment
	{
		TiXmlDocument xml;
		xml.Parse("<text>Dan & Tracie</text>");
		xml.Print(stdout);
	}
	{
		TiXmlDocument xml;
		xml.Parse("<text>Dan &foo; Tracie</text>");
		xml.Print(stdout);
	}
	*/

	#if defined( WIN32 ) && defined( TUNE )
	_CrtMemCheckpoint( &endMemState );
	//_CrtMemDumpStatistics( &endMemState );

	_CrtMemState diffMemState;
	_CrtMemDifference( &diffMemState, &startMemState, &endMemState );
	_CrtMemDumpStatistics( &diffMemState );
	#endif

	printf ("\nPass %d, Fail %d\n", gPass, gFail);
	return gFail;
}

#endif

#define SCREEN_WIDTH  480
#define SCREEN_HEIGHT 800
static void drawScene(int width, int height)
{
}
class SEDemo : public PVRShell
{
public:
    SEDemo()
    {
        //mPhysics = NULL;
    }
    virtual bool InitApplication();
    virtual bool InitView();
    virtual bool ReleaseView();
    virtual bool QuitApplication();
    virtual bool RenderScene();
private:
    void handleInput(int width, int height);
private:
    //SE_Physics* mPhysics;
    //EyeData eyeData;
};
bool SEDemo::InitApplication()
{
    //PVRShellSet(prefWidth, SCREEN_WIDTH);
    //PVRShellSet(prefHeight, SCREEN_HEIGHT);
    /*
    SE_ImageTableManager tableManager;
    SE_ImageTableSet* imageSet = new SE_ImageTableSet;
    tableManager.setTableSet(SE_StringID("test1"), imageSet);
    SE_ImageTable* imageTable = new SE_ImageTable;
    imageSet->setTable(SE_StringID("test2"), imageTable);
    imageTable->setItem(SE_StringID("test3"), SE_StringID("testid.png"));
    SE_StringID value;
    bool ret = tableManager.getValue("test1/test2/test3", value);
    */
    SE_Application::SE_APPID appid;
    appid.first = 137;
    appid.second = 18215879;
    SE_Application::getInstance()->setAppID(appid);
    SE_SystemCommandFactory* sf = new SE_SystemCommandFactory;
    SE_Application::getInstance()->registerCommandFactory("SystemCommand", sf);
    //SE_Init2D* c = new SE_Init2D(SE_Application::getInstance());
    //c->data = &eyeData;

    
    //std::string datapath = "e:\\model\\group_sky\\guangzhaotetu";//"D:\\model\\jme\\home\\newhome3";//"e:\\model\\testM";
    //std::string datapath = "e:\\codebase\\win_svn_base\\assets\\base\\backupmodel\\demohome";
    //std::string datapath = "E:/model/xinhome/coloreffect";
    //"D:\\model\\jme\\home\\newhome3";//"e:\\model\\testM";
   std::string datapath = "E:/codebase/win_svn_base/3DHome/assets/base/backupmodel/airplane";
    //std::string datapath = "e:\\model\\xin";
    std::string filename = "airplane";
    bool useAverageNormal = false;
    {
        SE_InitAppCommand* c = (SE_InitAppCommand*)SE_Application::getInstance()->createCommand("SE_InitAppCommand");
        c->dataPath = datapath;
        c->fileName = filename;
        SE_Application::getInstance()->postCommand(c);
    }   



    {
#if 0
        SE_CreateCBFCommand* c = (SE_CreateCBFCommand*)SE_Application::getInstance()->createCommand("SE_CreateCBFCommand");
        c->dataPath = datapath;
        c->sceneDataFile.push_back(filename); 

        //std::string simple = filename + "simple";
        //c->sceneDataFile.push_back(simple);
        c->averageNormal = useAverageNormal;
        SE_Application::getInstance()->postCommand(c);
#endif
    }


    SE_CreateSceneCommand *cs = (SE_CreateSceneCommand*)SE_Application::getInstance()->createCommand("SE_CreateSceneCommand");
    cs->dataPath = datapath;
    cs->fileName = filename;
    cs->xmlpath = "e:/codebase/win_svn_base/3DHome/assets/base/scene/scene_resource.xml";
    SE_Application::getInstance()->postCommand(cs);

    if(0)
    {
        std::string datapath = "E:/codebase/win_svn_base/3DHome/assets/base/pc";;//"D:\\model\\jme\\home\\newhome3";//"e:\\model\\testM";        
        std::string filename = "pc";
    
        SE_CreateSceneCommand *cs = (SE_CreateSceneCommand*)SE_Application::getInstance()->createCommand("SE_CreateSceneCommand");
        cs->dataPath = datapath;
        cs->fileName = filename;
        SE_Application::getInstance()->postCommand(cs);
    }

    
    return true;
}
class _CameraSetCondition : public SE_CommandExecuteCondition
{
public:
    bool isFulfilled()
    {
        if(SE_Application::getInstance()->getState() == RUNNING)
            return true;
        else
            return false;
    }
};

void cgh()
{
    TiXmlDocument doc;
	TiXmlDeclaration * decl = new TiXmlDeclaration( "1.0", "utf-8", "" );
    doc.LinkEndChild(decl);

	TiXmlElement * root = new TiXmlElement( "resource" );
    doc.LinkEndChild(root);    

    TiXmlElement * e_imageKeys = new TiXmlElement( "imageKeys" );
    root->LinkEndChild(e_imageKeys);  

    for(int i = 0; i < 3; ++i)
    {

        TiXmlElement * e_imagekey = new TiXmlElement( "imageKey" );
        e_imageKeys->LinkEndChild(e_imagekey);

        e_imagekey->SetAttribute("imageid","xxxx");
        e_imagekey->SetAttribute("path","/");
    }       
        
	
    doc.SaveFile( "e:/madeByHand.xml" );
}
bool SEDemo::InitView()
{
#ifdef TINYXML
    //testtiny();
    //cgh();
    
#endif
    int dwCurrentWidth = PVRShellGet (prefWidth);
    int dwCurrentHeight = PVRShellGet (prefHeight);
	LOGI("## width = %d, height = %d ###\n", dwCurrentWidth,dwCurrentHeight);
    SE_InitCameraCommand* c = (SE_InitCameraCommand*)SE_Application::getInstance()->createCommand("SE_InitCameraCommand");
    c->width = dwCurrentWidth;
    c->height = dwCurrentHeight;
    c->setCondition(new _CameraSetCondition);
    SE_Application::getInstance()->postCommand(c);

    SE_Application::getInstance()->start();
    return true;
}
bool SEDemo::ReleaseView()
{    
	SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
    if(!resourceManager)
    {
        return false;
    }

    resourceManager->releaseHardwareResource();

    //SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();
    //SE_SceneManager::_SceneSet sset = scm->getSceneSet();

    //    for(int i = 0; i < sset.size(); ++i)
    //    {            
    //        SE_Scene * scene = sset[i];
    //        if(!scene)
    //        {
    //            LOGI("Not found scene!!!!! \n");
    //            continue;
    //        }
    //        SE_Spatial* root = scene->getRoot();
    //        scene->removeSpatial(root);
    //        delete root;
    //    }

    delete SE_Application::getInstance();

    SE_ObjectFactory::factoryRelease();
    return true;
}
bool SEDemo::QuitApplication()
{
    
    return true;
}
static SE_Vector3f startPos;
static SE_CommonNode* groupNode = NULL;

struct _compare
{
    
    bool operator()(int right,int left)
    {
        if(left < right)
        {
            return false;
        }
	    return true;
    }
 
};

static SE_Animation *animation_a = NULL;
static int frameIndex = 0;
static int Framecount = 0;
void SEDemo::handleInput(int width, int height)
{
    static float prevPointer[2];
    static bool bPressed = false;
    int buttonState = PVRShellGet(prefButtonState);
    float* pointerLocation = (float*)PVRShellGet(prefPointerLocation);
    /*LOGI("## buttonstate = %d ##\n", buttonState);*/
    if(pointerLocation)
    {
        //LOGI("### pointer location = %f, %f ###\n", pointerLocation[0], pointerLocation[1]);//comment out by guohua
        prevPointer[0] = pointerLocation[0];
        prevPointer[1] = pointerLocation[1];
    }
    if((buttonState & ePVRShellButtonLeft))
    {
        SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
        if(c)
        {
            SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::DOWN, prevPointer[0] * width, prevPointer[1] * height);
            c->motionEvent = ke;
            SE_Application::getInstance()->postCommand(c);
        }
        bPressed = 1;
    }
    else if(bPressed)
    {
        SE_MotionEventCommand* c = (SE_MotionEventCommand*)SE_Application::getInstance()->createCommand("SE_MotionEventCommand");
        if(c)
        {
            SE_MotionEvent* ke = new SE_MotionEvent(SE_MotionEvent::UP, prevPointer[0] * width, prevPointer[1] * height);
            c->motionEvent = ke;
            SE_Application::getInstance()->postCommand(c);
        }
        bPressed = 0;
    }
    if(PVRShellIsKeyPressed(PVRShellKeyNameLEFT))
    {        

#if 0
        root->setNeedBlackWhiteColor(true);
        SE_Spatial* color = scm->findSpatialByName("ground00@group_house@home_basedata.cbf");
        color->setNeedBlackWhiteColor(false);
#endif
        

#if 0
        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();
        SE_Scene* scene = scm->getMainScene();
        scene->saveScene("e:/light.xml");

        scene->saveChildrenEffect("e:/geometryeffect.xml");
#endif
//SE_Spatial* guang = scm->findSpatialByName("group_email");
//        guang->clone(scm->getMainScene()->getRoot(),1);

#if 1
 SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();

 SE_Spatial* root = scm->getMainScene()->getRoot();


SE_Spatial* nav = scm->findSpatialByName("group_airplane");

//std::string status = nav->getChildrenStatus();
//nav->setNeedGenerateMirror(true);
//SE_Spatial*nav1 = nav->clone(root,1,false,status.c_str());
//
//
//nav1->setUserTranslateIncremental(SE_Vector3f(0,0,350));
//nav1->updateWorldTransform();
//
//nav->setNeedBlend(false);
 //SE_Spatial* ssss = scm->findSpatialByName("Plane06@group_navigation@navigation_basedata.cbf");
 //ssss->setNeedBlend(false);
 //ssss->setNeedForeverBlend(false);
        
        
        if( animation_a == NULL)
        {
           
            //animation_a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/email/animation.xml",false);
            animation_a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/3DHome/assets/base/navigation/animation.xml",false);
            //animation_a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/BackupModel/demohome/animation_book.xml",false);
            //animation_a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/BackupModel/demohome/animation_guang3.xml",false);
            //animation_a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/BackupModel/demohome/animation_word.xml",false);
            animation_a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/3DHome/assets/base/backupmodel/airplane/animation.xml",false);
        }
        else
        {   animation_a->run();
            animation_a->setReversePlay(false);
            
            animation_a->playFrameIndex(frameIndex);
            frameIndex++;
        }
        
#endif
#if 0
        static int i = 0;
        SE_AnimationManager* am = SE_Application::getInstance()->getAnimationManager();
        SE_AnimationID id = "group_shu";
        //SE_AnimationID id = "book_mesh_animation";
        //SE_AnimationID id = "clock";

        if(i == 0)
        {       

            SE_Animation *a = am->getAnimation(id);

            if(!a)
            {            
                //SE_Animation* a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/wallglobe/animation.xml");
                //SE_Animation* a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/BackupModel/book/animation.xml");
                //a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/clock/animation.xml");
               //a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/home/animation.xml");
                //a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/desk/animation.xml");
                //SE_Animation* a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/pc/animation.xml");
                //a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/picframe/animation.xml");
                //a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/picframe/animation_for_heng.xml");
                //a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/email/animation.xml");
                //a = SE_Application::getInstance()->getAnimationManager()->inflateAnimationFromXML("e:/codebase/win_svn_base/assets/base/BackupModel/demohome/animation.xml");
                if(a)
                {
                    a->run();

                }
            }
            else
            {
                if(!a->isRunning())
                {
                    a->setRunMode(SE_Animation::NOT_REPEAT);
                    a->run();
                }
            }
            i++;
        }
        else
        {

            i--;
            
            SE_Animation* a = am->getAnimation(id);
            if(a)
            {
                if(!a->isRunning())
                {
                    a->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
                    a->run();
                }
            }
        }
#endif

#if 0

        SE_Application::getInstance()->setFrameRate(1);

        SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
        SE_Spatial* a = sm->findByName("Box02@group_book@book_basedata.cbf");
        SE_Spatial* b = sm->findByName("Box01@group_book@book_basedata.cbf");
        SE_AnimationManager* am = SE_Application::getInstance()->getAnimationManager();

        float speed = 5;
        SE_Animation* animation = am->getAnimation(a->getSpatialName());
        if(!animation)
        {
            SE_GeometryDataID id = a->getSpatialName();
            SE_GeometryDataList* dl = SE_Application::getInstance()->getResourceManager()->getAnimationMeshList(id);
            animation = new SE_Animation();

            animation->setRunMode(SE_Animation::NOT_REPEAT);
            animation->setTimeMode(SE_Animation::SIMULATE);

            animation->setFrameNum(dl->mGeoDataList.size() - 1);
            animation->setTimePerFrame(1*speed);
            animation->setDuration((dl->mGeoDataList.size() - 1) * speed);

            am->addAnimation(animation,a->getSpatialName());
            SE_MeshAnimationListener* l = (SE_MeshAnimationListener*)SE_AnimationListenerFactory::createListener(MESH_ANIMATION);
            l->setAnimationSpatial(a);
            animation->addListener(l);
            animation->run();
        }
        else
        {
            animation->run();
        }


        SE_Animation* animation1 = am->getAnimation(b->getSpatialName());
        if(!animation1)
        {
            SE_GeometryDataID id = b->getSpatialName();
            SE_GeometryDataList* dl = SE_Application::getInstance()->getResourceManager()->getAnimationMeshList(id);
            animation1 = new SE_Animation();
            animation1->setRunMode(SE_Animation::NOT_REPEAT);
            animation1->setTimeMode(SE_Animation::SIMULATE);

            animation1->setFrameNum(dl->mGeoDataList.size() - 1);
            animation1->setTimePerFrame(1* speed);
            animation1->setDuration((dl->mGeoDataList.size() - 1) * speed);

            am->addAnimation(animation1,b->getSpatialName());
            SE_MeshAnimationListener* l1 = new SE_MeshAnimationListener(b);
            animation1->addListener(l1);
            animation1->run();
        }
        else
        {
            animation1->run();
        }       
       
#endif
#if 0
        static int i = 0;
        if(i == 0)
        {
#if 0       
        SE_AddNewCbfCommand* c1 = (SE_AddNewCbfCommand*)SE_Application::getInstance()->createCommand("SE_AddNewCbfCommand");

        //set rotate angle per ticket
        
        c1->dataPath = "E:\\codebase\\win_svn_base\\assets\\base\\desk";        

        //set spatialid. this is minute hand spatial
        //std::string model_name;
        //char buff[256];
        //sprintf(buff,"%d",i);
        //model_name = "home" + std::string(buff);
        //c1->mCbfFileNameID = model_name.c_str();
        c1->mCbfFileNameID = "desktop";

        //post this command to command queue
        SE_Application::getInstance()->postCommand(c1);
        i = 2;
#endif
        }
        else
        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("group_email");
            

            a->setUserTranslate(SE_Vector3f(0,0,130));
            a->updateWorldTransform();
            a->updateBoundingVolume();
            i = 0;
        }
#endif


        
#if 0
        SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
        if(!resourceManager)
        {
            return;
        }        
        SE_Scene* scene =  SE_Application::getInstance()->getSceneManager()->getScene(SE_FRAMEBUFFER_SCENE, SE_StringID("home"));

        SE_Spatial* spatial = scene->findSpatialByName("group_house");
        SE_Spatial* rs = scene->removeSpatial(spatial);
        delete rs;
        
        resourceManager->setDataPath("e:\\model\\home5");
        SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->findSpatialByName("group_house");
        if (!root)
        {
              root = new SE_CommonNode(SE_ID::createSpatialID());
              root->setSpatialName("group_house");
              scene->addSpatial(scene->getRoot(), root);
        }        
        SE_Spatial* s = resourceManager->loadScene("home");
        s->replaceChildParent(root);
        delete s;
        resourceManager->loadBaseData("home");
        scene->inflate();
        root = scene->getRoot();
        root->updateWorldTransform();
        root->updateBoundingVolume();
        root->updateWorldLayer();
#endif
#if 0
        SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
        SE_Scene* scene = new SE_Scene("home1");  

        SE_Camera* camera = sceneManager->getMainScene()->getCamera();
        scene->setCamera(camera); 

        sceneManager->pushBack(scene->getType(), scene);    
        sceneManager->setMainScene("home1");
        scene->loadResource("e:\test","home1");   
#endif
  
#if 0
/*
        static bool a = false;
        if(a)
        {
            
            group->setRenderType(SE_FADEINOUT_RENDER);
            group->setShaderType(SE_FADEINOUT_SHADER);
            group->setAlpha(0.5);
            a = false;
        }
        else
        {
           
            group->setRenderType(SE_FADEINOUT_RENDER);
            group->setShaderType(SE_FADEINOUT_SHADER);
            group->setAlpha(0.5);
            a= true;
        }
        */

        //SE_Spatial* root = group->getParent();

        //group->clone(root);
        
        //scm->getMainScene()->moveCollisionDetect("Box001@vb_basedata.cbf",SE_Vector3f(0,20,0));

        //SE_RenderManager* srdm = SE_Application::getInstance()->getRenderManager();
        //srdm->setRenderSortType(SE_RenderManager::SORT_BY_RESOURCE);

        //group->setLocalLayer(SE_Layer(maxlayer + 10));
        //group->updateWorldLayer();

        //SE_Quat rotate(10,SE_Vector3f(0,0,1));
        //root->setUserRotateIncremental(rotate);
        //root->updateWorldTransform();

        //group->setUserScaleIncremental(SE_Vector3f(1,2,1));
        //group->updateWorldTransform();
        

        /*
        SE_SimObject * src = SE_Application::getInstance()->getSimObjectManager()->findByName("Box001@vb_basedata.cbf");
 
        for(int i = 0; i < src->getSurfaceNum(); ++i)
        {
            src->getMesh()->getSurface(i)->setAlpha(0.5);
            src->getMesh()->getSurface(i)->setRendererID(FADEINOUT_RENDERER);
            src->getMesh()->getSurface(i)->setProgramDataID(FADEINOUT_SHADER);

        }
        */
#endif



#if 0
      
        /*

        SE_Matrix3f scaleM;
        scaleM.identity();

        SE_Matrix4f transform;
        transform.identity();

        scaleM.setScale(2.0f,2.0f,2.0f);
        transform.set(scaleM,SE_Vector3f(0,0,0));

        //group->setPostMatrix(group->getPostMatrix().mul(transform));
        group->setLocalScale(SE_Vector3f(2,2,2));
         //update 


        scm->getMainScene()->getRoot()->updateWorldTransform();
        scm->getMainScene()->getRoot()->updateBoundingVolume();
        */

#endif       

        

#if 0
        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();
        SE_Scene* scene = scm->getMainScene();
        SE_Spatial* root = scene->getRoot();         

        SE_Spatial* sp = NULL;
int num = 1;
SE_ResourceManager* rm = SE_Application::getInstance()->getResourceManager();

SE_ImageDataID imgid = "desk_basedata.cbf\0";
        if(1)
        {           
            

            std::string name;
            name = "my1";            

            SE_ObjectCreator* creator = new SE_ObjectCreator();

            //creator->setImageDataID(imgid);

            creator->setObjectName("my1");
            int bvType = 1;
            creator->setBvType(bvType);

            creator->setIfNeedBlending(false);
            creator->setIfNeedDepthTest(true);
            
            float localTranslate[3] = {0, 0,0};
            
            float localRotate[4] = {0, 0,0,1};
            
            float localScale[3] = {1,1,1};
            

            SE_Vector3f v = SE_Vector3f(localTranslate[0], localTranslate[1], localTranslate[2]);
            SE_Quat q;
            q.set(localRotate[0], SE_Vector3f(localRotate[1], localRotate[2], localRotate[3]));
            SE_Vector3f s = SE_Vector3f(localScale[0], localScale[1], localScale[2]);
            creator->setLocalRotate(q);
            creator->setLocalTranslate(v);
            creator->setLocalScale(s);

            
            SE_Vector3f uv = SE_Vector3f(0, 0, 0);

            SE_Quat uq;
            uq.set(0, SE_Vector3f(0, 0, 0));

            SE_Vector3f us = SE_Vector3f(1, 1, 1);

            creator->setUserRotate(uq);
            creator->setUserTranslate(uv);
            creator->setUserScale(us);

            SE_Vector3f* va = new SE_Vector3f[3];            
            
            va[0].x = -50;
            va[0].y = -150;
            va[0].z = -50;

            va[1].x = 50;
            va[1].y = -150;
            va[1].z = -50;

            va[2].x = 0;
            va[2].y = -150;
            va[2].z = 100;
            creator->setVertexArray(va,3);

            SE_Vector3i *via = new SE_Vector3i[3];
            via[0].x=0;
            via[0].y=1;
            via[0].z=2;            
            creator->setVertexIndexArray(via,3);

           // SE_Vector2f* tv = new SE_Vector2f[12];
            //creator->setTextureCoorArray(tv,12);

            /*SE_Vector3i* tia = new SE_Vector3i[12];
            tia[0].x = 9;
            tia[0].y = 11;
            tia[0].z = 10;

            tia[1].x = 10;
            tia[1].y = 8;
            tia[1].z = 9;

            tia[2].x = 8;
            tia[2].y = 9;
            tia[2].z = 11;

            tia[3].x = 11;
            tia[3].y = 10;
            tia[3].z = 8;

            tia[4].x = 4;
            tia[4].y = 5;
            tia[4].z = 7;

            tia[5].x = 7;
            tia[5].y = 6;
            tia[5].z = 4;

            tia[6].x = 0;
            tia[6].y = 1;
            tia[6].z = 3;

            tia[7].x = 3;
            tia[7].y = 2;
            tia[7].z = 0;

            tia[8].x = 4;
            tia[8].y = 5;
            tia[8].z = 7;

            tia[9].x = 7;
            tia[9].y = 6;
            tia[9].z = 4;

            tia[10].x = 0;
            tia[10].y = 1;
            tia[10].z = 3;

            tia[11].x = 3;
            tia[11].y = 2;
            tia[11].z = 0;

            creator->setTextureCoorIndexArray(tia,12);*/


            int* faceta = new int[1];
            for(int i = 0; i < 1; ++i)
            {
                faceta[i] = i;
            }
            creator->setFacetIndexArray(faceta,1);

            float c = 1.0/(1+1);
            SE_Vector3f color = SE_Vector3f(1,0,0);
            creator->setColor(color);
            creator->setShaderType("default_shader");
            creator->setRenderType("default_renderer");

            creator->setIfNeedBlending(true);

            SE_Spatial* spatial = creator->create(scene, root);
            spatial->setVisible(true);
            spatial->setSpatialName(name.c_str());

            spatial->setAlpha(1.0);
            root->updateWorldTransform();
            root->updateBoundingVolume();
            
            delete creator;
            
        }
        

        if(1)
        {           
            

            std::string name;
            name = "my0";            

            SE_ObjectCreator* creator = new SE_ObjectCreator();

            //creator->setImageDataID(imgid);

            creator->setObjectName("my0");
            int bvType = 1;
            creator->setBvType(bvType);

            creator->setIfNeedBlending(false);
            creator->setIfNeedDepthTest(true);
            
            float localTranslate[3] = {0, 0,0};
            
            float localRotate[4] = {0, 0,0,1};
            
            float localScale[3] = {1,1,1};
            

            SE_Vector3f v = SE_Vector3f(localTranslate[0], localTranslate[1], localTranslate[2]);
            SE_Quat q;
            q.set(localRotate[0], SE_Vector3f(localRotate[1], localRotate[2], localRotate[3]));
            SE_Vector3f s = SE_Vector3f(localScale[0], localScale[1], localScale[2]);
            creator->setLocalRotate(q);
            creator->setLocalTranslate(v);
            creator->setLocalScale(s);

            
            SE_Vector3f uv = SE_Vector3f(0, 0, 0);

            SE_Quat uq;
            uq.set(0, SE_Vector3f(0, 0, 0));

            SE_Vector3f us = SE_Vector3f(1, 1, 1);

            creator->setUserRotate(uq);
            creator->setUserTranslate(uv);
            creator->setUserScale(us);

            SE_Vector3f* va = new SE_Vector3f[3];            
            
            va[0].x = -10;
            va[0].y = -150;
            va[0].z = -50;

            va[1].x = 90;
            va[1].y = -150;
            va[1].z = -50;

            va[2].x = 40;
            va[2].y = -150;
            va[2].z = 100;
            creator->setVertexArray(va,3);

            SE_Vector3i *via = new SE_Vector3i[3];
            via[0].x=0;
            via[0].y=1;
            via[0].z=2;            
            creator->setVertexIndexArray(via,3);

           // SE_Vector2f* tv = new SE_Vector2f[12];
            //creator->setTextureCoorArray(tv,12);

            /*SE_Vector3i* tia = new SE_Vector3i[12];
            tia[0].x = 9;
            tia[0].y = 11;
            tia[0].z = 10;

            tia[1].x = 10;
            tia[1].y = 8;
            tia[1].z = 9;

            tia[2].x = 8;
            tia[2].y = 9;
            tia[2].z = 11;

            tia[3].x = 11;
            tia[3].y = 10;
            tia[3].z = 8;

            tia[4].x = 4;
            tia[4].y = 5;
            tia[4].z = 7;

            tia[5].x = 7;
            tia[5].y = 6;
            tia[5].z = 4;

            tia[6].x = 0;
            tia[6].y = 1;
            tia[6].z = 3;

            tia[7].x = 3;
            tia[7].y = 2;
            tia[7].z = 0;

            tia[8].x = 4;
            tia[8].y = 5;
            tia[8].z = 7;

            tia[9].x = 7;
            tia[9].y = 6;
            tia[9].z = 4;

            tia[10].x = 0;
            tia[10].y = 1;
            tia[10].z = 3;

            tia[11].x = 3;
            tia[11].y = 2;
            tia[11].z = 0;

            creator->setTextureCoorIndexArray(tia,12);*/


            int* faceta = new int[1];
            for(int i = 0; i < 1; ++i)
            {
                faceta[i] = i;
            }
            creator->setFacetIndexArray(faceta,1);

            float c = 1.0/(1+1);
            SE_Vector3f color = SE_Vector3f(1,0,0);
            creator->setColor(color);
            creator->setShaderType("default_shader");
            creator->setRenderType("default_renderer");

            creator->setIfNeedBlending(true);

            SE_Spatial* spatial = creator->create(scene, root);
            spatial->setVisible(true);
            spatial->setSpatialName(name.c_str());

            spatial->setAlpha(1.0);
            root->updateWorldTransform();
            root->updateBoundingVolume();
            
            delete creator;
        }

        if(1)
        {
            
            std::string name;
            name = "my2";            

            SE_ObjectCreator* creator = new SE_ObjectCreator();

            //creator->setImageDataID(imgid);

            creator->setObjectName("my2");
            int bvType = 1;
            creator->setBvType(bvType);

            creator->setIfNeedBlending(false);
            creator->setIfNeedDepthTest(true);
            
            float localTranslate[3] = {0, 0,0};
            
            float localRotate[4] = {0, 0,0,1};
            
            float localScale[3] = {1,1,1};
            

            SE_Vector3f v = SE_Vector3f(localTranslate[0], localTranslate[1], localTranslate[2]);
            SE_Quat q;
            q.set(localRotate[0], SE_Vector3f(localRotate[1], localRotate[2], localRotate[3]));
            SE_Vector3f s = SE_Vector3f(localScale[0], localScale[1], localScale[2]);
            creator->setLocalRotate(q);
            creator->setLocalTranslate(v);
            creator->setLocalScale(s);

            
            SE_Vector3f uv = SE_Vector3f(0, 0, 0);

            SE_Quat uq;
            uq.set(0, SE_Vector3f(0, 0, 0));

            SE_Vector3f us = SE_Vector3f(1, 1, 1);

            creator->setUserRotate(uq);
            creator->setUserTranslate(uv);
            creator->setUserScale(us);

            SE_Vector3f* va = new SE_Vector3f[4];
            
            va[0].x = -150;
            va[0].y = -300;
            va[0].z =  -50;

            va[1].x = 150;
            va[1].y = -300;
            va[1].z = -50;

            va[2].x = 150;
            va[2].y = 0;
            va[2].z = -50;

            va[3].x = -150;
            va[3].y = 0;
            va[3].z = -50;
            creator->setVertexArray(va,4);

            SE_Vector3i *via = new SE_Vector3i[6];
            via[0].x=0;
            via[0].y=1;
            via[0].z=2; 

            via[1].x=2;
            via[1].y=3;
            via[1].z=0;
            creator->setVertexIndexArray(via,6);

           // SE_Vector2f* tv = new SE_Vector2f[12];
            //creator->setTextureCoorArray(tv,12);

            /*SE_Vector3i* tia = new SE_Vector3i[12];
            tia[0].x = 9;
            tia[0].y = 11;
            tia[0].z = 10;

            tia[1].x = 10;
            tia[1].y = 8;
            tia[1].z = 9;

            tia[2].x = 8;
            tia[2].y = 9;
            tia[2].z = 11;

            tia[3].x = 11;
            tia[3].y = 10;
            tia[3].z = 8;

            tia[4].x = 4;
            tia[4].y = 5;
            tia[4].z = 7;

            tia[5].x = 7;
            tia[5].y = 6;
            tia[5].z = 4;

            tia[6].x = 0;
            tia[6].y = 1;
            tia[6].z = 3;

            tia[7].x = 3;
            tia[7].y = 2;
            tia[7].z = 0;

            tia[8].x = 4;
            tia[8].y = 5;
            tia[8].z = 7;

            tia[9].x = 7;
            tia[9].y = 6;
            tia[9].z = 4;

            tia[10].x = 0;
            tia[10].y = 1;
            tia[10].z = 3;

            tia[11].x = 3;
            tia[11].y = 2;
            tia[11].z = 0;

            creator->setTextureCoorIndexArray(tia,12);*/


            int* faceta = new int[2];
            for(int i = 0; i < 2; ++i)
            {
                faceta[i] = i;
            }
            creator->setFacetIndexArray(faceta,2);

            float c = 1.0/(1+1);
            SE_Vector3f color = SE_Vector3f(0,1,0);
            creator->setColor(color);
            creator->setShaderType("default_shader");
            creator->setRenderType("default_renderer");

            creator->setIfNeedBlending(true);

            SE_Spatial* spatial = creator->create(scene, root);
            spatial->setVisible(true);
            spatial->setSpatialName(name.c_str());

            spatial->setAlpha(1.0);
            root->updateWorldTransform();
            root->updateBoundingVolume();
            
            spatial->setEnableCullFace(false);
            delete creator;
        }

        SE_Spatial* sanjiao = scm->findSpatialByName("my1");
        sanjiao->setNeedGenerateMirror(true);
        sanjiao->setMirrorInfo("my2");

        SE_Spatial* sanjiao0 = scm->findSpatialByName("my0");
        sanjiao0->setNeedGenerateMirror(true);
        sanjiao0->setMirrorInfo("my2");



         SE_Spatial* fangkuai = scm->findSpatialByName("my2");
        fangkuai->setNeedRenderMirror(true);
        
#endif
        /*
        SE_BoxPrimitive* boxPrimitive = NULL;
        SE_PrimitiveID boxPrimitiveID;
        SE_BoxPrimitive::create(SE_Vector3f(1, 1, 1), boxPrimitive, boxPrimitiveID);
        //boxPrimitive->SE_ImageData* imageData = SE_Application::getInstance()->getResourceManager()->getImageData("TVscreen");
        //primitive->setImageData(imageData, SE_Texture::TEXTURE0, NOT_OWN);
        boxPrimitive->setImageData(SE_BoxPrimitive::ALL, imageData, SE_Texture::TEXTURE0, NOT_OWN);
        boxPrimitive->createMesh(meshArray, meshNum);
        SE_SpatialID groupSpatialID = SE_Application::getInstance()->createCommonID();
        groupNode = new SE_CommonNode(groupSpatialID, root);
        root->addChild(groupNode);
        SE_Vector3f v = camera->getLocation();
        v = v + SE_Vector3f(0, 25, 0);
        //v = SE_Vector3f(0, -50, v.z);
        groupNode->setLocalTranslate(v);
        groupNode->setLocalRotate(q);
        for(int i = 0 ; i < meshNum ; i++)
        {
            SE_Mesh* mesh = meshArray[i];
            SE_SpatialID spatialID = SE_Application::getInstance()->createCommonID();
            SE_Geometry* geometry = new SE_Geometry(spatialID, groupNode);
            groupNode->addChild(geometry);
            SE_MeshSimObject* simObj = new SE_MeshSimObject(mesh, OWN);
            simObj->setName("rect primitive");
            geometry->attachSimObject(simObj);
        }
        groupNode->updateWorldTransform();
        */       

    }
    else if(PVRShellIsKeyPressed(PVRShellKeyNameRIGHT))
    {

#if 0
        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();


        SE_Spatial* home = scm->findSpatialByName("group_house");
        
        std::string ssss = home->getChildrenStatus();


        SE_Spatial* sanjiao = scm->findSpatialByName("group_house");

        SE_Spatial* root = scm->getMainScene()->getRoot();

        sanjiao->clone(root,1,false,ssss.c_str());

        SE_Spatial* sanjiao1 = scm->findSpatialByName("Plane29@group_house@ceshi_basedata.cbf",1);
        sanjiao1->setUserTranslateIncremental(SE_Vector3f(-20,-500,0));
        sanjiao1->updateWorldTransform();
#endif

       /* SE_Spatial* jing = scm->findSpatialByName("my2");
        scm->getMainScene()->removeSpatial(jing);
        delete jing;*/
        /*SE_Spatial* my1 = scm->findSpatialByName("my1",1);
        scm->getMainScene()->removeSpatial(my1);
        delete my1;*/

#if 1
        static int i = 0;


        if(Framecount == 0)
        {
            i = 0;
            
           Framecount= animation_a->getCurrentFrame();
        }
        

        //animation_a->setRunMode(SE_Animation::REVERSE_NOT_REPEAT);
        animation_a->run();
        animation_a->setReversePlay(true);
        animation_a->playFrameIndex(frameIndex);
        frameIndex++;
#endif

                       
#if 0 

        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Box001@mirror_basedata.cbf");
            a->setNeedGenerateMirror(true);            
            a->setMirrorInfo("m1@mirror_basedata.cbf",SE_XY);
        }

        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Box002@mirror_basedata.cbf");
            a->setNeedGenerateMirror(true);  
            a->setMirrorInfo("m2@mirror_basedata.cbf",SE_XY);
        }        

        SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
        SE_Spatial* a = sm->findByName("m1@mirror_basedata.cbf");        
        a->setNeedRenderMirror(true);        
        a->setAlpha(0.5);

        SE_Spatial* b = sm->findByName("m2@mirror_basedata.cbf");        
        b->setNeedRenderMirror(true);        
        b->setAlpha(0.6);
        
        //a->setRenderType("mirror_renderer");
        //a->setShaderType("mirror_shader");  
#endif
#if 0
        static int b = 1;
        if(b == 0)
        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("desktop@desktop_basedata.cbf");
            

            SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();
            scm->getMainScene()->removeSpatial(a);
            delete a;
            b = 1;
        }
        else
        {
            SE_AddNewCbfCommand* c1 = (SE_AddNewCbfCommand*)SE_Application::getInstance()->createCommand("SE_AddNewCbfCommand");

            //set rotate angle per ticket
            
            c1->dataPath = "E:\\model\\newhemo";        

            //set spatialid. this is minute hand spatial
            //std::string model_name;
            //char buff[256];
            //sprintf(buff,"%d",i);
            //model_name = "home" + std::string(buff);
            //c1->mCbfFileNameID = model_name.c_str();
            c1->mCbfFileNameID = "shadow";

            //post this command to command queue
            SE_Application::getInstance()->postCommand(c1);
        }
        /*
        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Box001@box_t_basedata.cbf");
            int num = a->getCurrentAttachedSimObj()->getMesh()->getSurfaceNum();
            for(int i = 0; i < num; ++i)
            {
                a->getCurrentAttachedSimObj()->getMesh()->getSurface(i)->setNeedUseFbo(true);
                a->getCurrentAttachedSimObj()->getMesh()->getSurface(i)->setFboReplaceCurrentTexture(false);
                a->getCurrentAttachedSimObj()->getMesh()->getSurface(i)->setFboReplaceTextureIndex(0);
            }
        }
        */
#endif

        /*
        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Sphere001@box_t_basedata.cbf");
            int num = a->getCurrentAttachedSimObj()->getMesh()->getSurfaceNum();
            for(int i = 0; i < num; ++i)
            {
                a->getCurrentAttachedSimObj()->getMesh()->getSurface(i)->setNeedUseFbo(true);
            }
        }
        */
#if 0
        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();
        SE_Scene* scene = scm->getMainScene();
        SE_Spatial* root = scene->getRoot();

        SE_Spatial* spmirror = NULL;       
        spmirror = scene->findSpatialByName("Box001@pc_basedata.cbf");
        spmirror->setUserTranslateIncremental(SE_Vector3f(0,0,-5));
        spmirror->updateWorldTransform();
        spmirror->updateBoundingVolume();


        SE_Spatial* sp = NULL;
        sp = scene->findSpatialByName("Group_PC");        

        sp->setUserTranslateIncremental(SE_Vector3f(0,0,-5));
        sp->updateWorldTransform();
        sp->updateBoundingVolume();
#endif
#if 0
            SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();        
            SE_Scene* main = scm->getMainScene();
            SE_Spatial* oldsp = main->findSpatialByName("GlobalEarth@group_browser@earth_basedata.cbf");

            main->removeSpatial(oldsp);
            delete oldsp;
            main->getRoot()->updateWorldTransform();
            main->getRoot()->updateBoundingVolume();
#endif

            
        #if 0

            SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();        
            SE_Scene* main = scm->getMainScene();
            //SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            //SE_Spatial* a = sm->findByName("Sphere001@home_basedata.cbf");
            //a->setRenderType("simplelighting_renderer");
            //a->setShaderType("simplelighting_shader");

            //SE_Spatial* b = sm->findByName("light0_scene@home_basedata.cbf");
            //b->setRenderType("simplelighting_renderer");
            //b->setShaderType("simplelighting_shader");
        
            /*SE_Light* light2 = new SE_Light();
            light2->setLightType(SE_Light::SE_POINT_LIGHT);
            light2->setPointLightPos(SE_Vector3f(100,-50,50));
            light2->setLightColor(SE_Vector3f(0,1,1.0));
            light2->setDirLightStrength(1.0);
            light2->setLightName("light2");*/

            /*SE_Light* light1 = new SE_Light();
            light1->setLightType(SE_Light::SE_POINT_LIGHT);
            light1->setPointLightPos(SE_Vector3f(-100,-50,50));
            light1->setLightColor(SE_Vector3f(1,1,1));
            light1->setLightName("light1");*/


            if(!main->getLight("light0"))
            {           
                SE_Light* light0 = new SE_Light();
                light0->setLightType(SE_Light::SE_SPOT_LIGHT);            
                light0->setLightPos(SE_Vector3f(0,500,300)); 
                light0->setLightDir(SE_Vector3f(0,0,-1));
                light0->setSpotLightCutOff(0.90);
                light0->setSpotLightExp(1.0);
                light0->setConstantAttenuation(1.0);
                light0->setLinearAttenuation(0);
                light0->setQuadraticAttenuation(0);
                light0->setAttenuation(0.0);
                light0->setLightName("light0");   
                main->addLightToScene(light0);
            }
            
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("ground@group_house@home_basedata.cbf");
            a->addLightNameToList("light0");
           
            
            /*SE_Light* light1 = new SE_Light();
            light1->setLightType(SE_Light::SE_POINT_LIGHT);            
            light1->setPointLightPos(SE_Vector3f(-50,0,50));
            light1->setConstantAttenuation(1.0);
            light1->setLinearAttenuation(0);
            light1->setQuadraticAttenuation(0);
            light1->setLightName("light1");*/

            
            main->setLowestEnvBrightness(0.0);
            //main->addLightToScene(light2,"Sphere02@group_globe@globe_basedata.cbf");
            
            //main->addLightToScene(light1);
            //main->addLightToScene(light2);
            /*if(!main->addLightToScene(light1,"Object03@group_house@home_basedata.cbf"))
            {
                delete light1;
            }
            if(!main->addLightToScene(light0))
            {
                delete light0;
            }

            if(!main->addLightToScene(light2))
            {
                delete light2;
            }*/
            //main->addLightToScene(light2);
            //main->changeSceneShader("simplelighting_shader","simplelighting_renderer");

#endif
        /*
        SE_SceneManager* sceneManager = SE_Application::getInstance()->getSceneManager();
        SE_Scene* se = new SE_Scene("vb");
        sceneManager->pushBack(SE_POST_EFFECT_SCENE, se);
        se->setIsTranslucent(true);
        se->loadResource("e:\\model\\testM\\","vb"); 
        SE_Camera* camera1 = new SE_MotionEventCamera();

        int width = 640;
        int height = 480;
        SE_Vector3f location(50,0,0);//vb
        SE_Vector3f zAxis(1, 0, 0);    
        SE_Vector3f up(0, 0, 1);    
        camera1->create(location, zAxis, up, 60.0f,((float)height)/ width, 1.0f, 1000.0f);
        camera1->setViewport(0, 0, width, height);

        se->setCamera(camera1); 
        */


#if 0
        SE_DeleteObjectCommand* c1 = (SE_DeleteObjectCommand*)SE_Application::getInstance()->createCommand("SE_DeleteObjectCommand");

        //set rotate angle per ticket
        
        c1->mObjectName = "Box001";

        //set spatialid. this is minute hand spatial
        //std::string model_name;
        //char buff[256];
        //sprintf(buff,"%d",i);
        //model_name = "home" + std::string(buff);
        //c1->mCbfFileNameID = model_name.c_str();
        //c1->mCbfFileNameID = "detail";

        //post this command to command queue
        SE_Application::getInstance()->postCommand(c1);

        //SE_UnLoadSceneCommand* c1 = (SE_UnLoadSceneCommand*)SE_Application::getInstance()->createCommand("SE_UnLoadSceneCommand");
        //SE_Application::getInstance()->postCommand(c1);

        //SE_SimObject* sim = SE_Application::getInstance()->getSimObjectManager()->findByName("rect primitive");
        
        //SE_Application::getInstance()->getSceneManager()->removeSpatial(sim->getSpatial()->getSpatialID());

        //SE_Application::getInstance()->getSceneManager()->updateSpatialIDMap();

        //SE_SimObject* ori_sim = SE_Application::getInstance()->getSimObjectManager()->findByName("rect primitive");
        LOGI("$$hiden a particle.\n");
        /*
        mPhysics = new SE_Physics;
        mPhysics->setStartPos(startPos);
        mPhysics->initPhysics();
        LOGI("## right ##\n");
        */

        /*
        SE_MotionEventController * controller = (SE_MotionEventController *)SE_Application::getInstance()->getInputManager()->getCurrentMotionEventObserve();
 
        controller->getCameraController()->returnToBack();
        */ 
#endif
    
        

    }
    else if(PVRShellIsKeyPressed(PVRShellKeyNameUP))
    {        

         frameIndex = 0;
         Framecount = 0;
#if 0
        SE_RenderTargetManager* rtm = SE_Application::getInstance()->getRenderTargetManager();

        //rtm->getDefaultRenderTarget()->saveToFile("e:\\savefile0.png");
        ////rtm->getShadowTarget()->saveToFile("e:\\savefile_shadow.png");
        //rtm->getDownSampleTarget()->saveToFile("e:\\savefile_downsample.png");
        //rtm->getBlurVTarget()->saveToFile("e:\\savefile_blurV.png");
        //rtm->getBlurHTarget()->saveToFile("e:\\savefile_blurH.png");
        //rtm->getDofGenTarget()->saveToFile("e:\\savefile_dof.png");
        rtm->getDrawScreenToTarget()->saveToFile("e:\\drawscreentotarget.png");
        rtm->getColorEffectTarget()->saveToFile("e:\\colortarget.png");
#endif

        /*SE_Camera* c = SE_Application::getInstance()->getCurrentCamera();

        if(!c->isNeedDof())
        {
            c->setNeedDof(true);
        }
        else
        {
            c->setNeedDof(false);
        }
        SE_Vector4f para(100,500,1000,1.0);
        c->setDofPara(para);


        LOGI("");*/

        /*SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();
        SE_Scene* mainScene  = scm->getMainScene();
        SE_Light::LightPara* para = mainScene->updateSceneLight("light1");
        para->mPointLightPos = SE_Vector3f(500,0,200);*/
        //mainScene->removeLight("light1");
        /*SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
        SE_Spatial* a = sm->findByName("Object02w@group_house@home_basedata.cbf");
        
        std::vector<std::string> nl;
        a->getLightsNameList(nl);

        for(int i = 0; i < nl.size(); ++i)
        {
            LOGI("light name is %s\n",nl[i].c_str());
            a->removeLight(nl[i].c_str());
        }*/
        
        /*a->setNeedUVAnimation(true);
        static int i = 0;
        SE_Vector2f offset = SE_Vector2f(0.05 * i, 0 * i);
        a->setTexCoordOffset(offset);
        i++;
        if(i > 200)
        {
            i = 0;
        }*/
         
#if 0
        SE_SaveSceneCommand *css = (SE_SaveSceneCommand*)SE_Application::getInstance()->createCommand("SE_SaveSceneCommand");
        css->mOutFileName = "phone";//outFileName;
        SE_Application::getInstance()->postCommand(css);
#endif
        
        
        
#if 0
        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Object04@group_house@home_basedata.cbf");
            a->setNeedGenerateShadow(false);            
            
        }

        {
            
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Object43@group_house@home_basedata.cbf");
            a->setNeedGenerateShadow(false);        
            
        }

        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Object34@group_house@home_basedata.cbf");
            a->setNeedGenerateShadow(false);        
        }

        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Object006@group_house@home_basedata.cbf");
            a->setNeedGenerateShadow(false);        
        }

        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Object37@group_house@home_basedata.cbf");
            a->setNeedGenerateShadow(false);        
        }

        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Object42@group_house@home_basedata.cbf");
            a->setNeedGenerateShadow(false);        
        }

        {
            SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* a = sm->findByName("Object77@group_house@home_basedata.cbf");
            a->setNeedGenerateShadow(false);        
        }
#endif
#if 0
        static int i = 0;
        if(i == 0)
        {
            SE_ResourceManager* resourceManager = SE_Application::getInstance()->getResourceManager();
            SE_Scene* scene =  SE_Application::getInstance()->getSceneManager()->getMainScene();
            resourceManager->setDataPath("E:\\codebase\\win_svn_base\\assets\\base\\desk");
            SE_Spatial* root = scene->getRoot(); 
            SE_Spatial* s = resourceManager->loadScene("desk");
            if (s) 
            {
               s->replaceChildParent(root);
               delete s;
               resourceManager->loadBaseData("desk");
               scene->inflate();
               root = scene->getRoot();
               root->updateWorldTransform();
               root->updateBoundingVolume();
               root->updateWorldLayer();
           }

            i++;
        }
        else
        {
            SE_Spatial* spatial = SE_Application::getInstance()->getSpatialManager()->findByName("group_desk");
            if(spatial)
	        {
	            SE_Scene*  scene = spatial->getScene();
	            SE_Spatial* rs = scene->removeSpatial(spatial);
                if (rs)
                {
                    delete rs;
                }
	        }

            i = 0;
        }
#endif
                
        #if 0
        SE_Vector3f* va = new  SE_Vector3f[4];
            SE_Vector3i* faces = new SE_Vector3i[2];
            int* facet = new int[2];

            va[0].set(20,20,0);
            va[1].set(20,0,0);
            va[2].set(20,0,150);
            va[3].set(20,20,150);        

            faces[0].x = 0;
            faces[0].y = 1;
            faces[0].z = 2;

            faces[1].x = 0;
            faces[1].y = 2;
            faces[1].z = 3;       

            for(int i=0;i<2;i++)
            {
                facet[i]=i;
            }

            SE_Vector2f* texVertex = new SE_Vector2f[4];
            SE_Vector3i* texFaces = new SE_Vector3i[2];
            texVertex[0] = SE_Vector2f(0, 0);
            texVertex[1] = SE_Vector2f(1, 0);
            texVertex[2] = SE_Vector2f(1, 1);
            texVertex[3] = SE_Vector2f(0, 1);

            SE_Vector3i* mfaces = new SE_Vector3i[2];
            mfaces[0].x = 0;
            mfaces[0].y = 1;
            mfaces[0].z = 2;

            mfaces[1].x = 0;
            mfaces[1].y = 2;
            mfaces[1].z = 3;       

            texFaces = mfaces;

            int wrapS = 1;
        int wrapT = 1;
    //int* facet = new int;

        SE_ImageData* imgd = SE_ImageCodec::load("D:\\long.png");
        //SE_ImageDataID imageDataId = SE_ID::createImageDataID("D:\\flares.png");

        SE_ImageDataID imageDataId("D:\\long.png");
           //std::string tidstr = "D:\\long.png";
           //SE_ImageDataID imageDataId = tidstr.c_str();
         SE_ResourceManager *resourceManager = SE_Application::getInstance()->getResourceManager();
            resourceManager->setImageData(imageDataId,imgd);
            //SE_ImageDataID tid = tidstr.c_str();


        SE_ImageDataID* imageIDArray = new SE_ImageDataID[1];
        imageIDArray[0] = imageDataId;

        SE_TextureCoordDataID textureCoordDataID = SE_ID::createTextureCoordDataID("Xccc");

      SE_GeometryData* geomData = new SE_GeometryData;
      geomData->setVertexArray(va,4 );
      geomData->setFaceArray(faces,2);

      SE_TextureCoordData* texCoordData = new SE_TextureCoordData;
      SE_TextureUnit* texUnit = new SE_TextureUnit();

      texCoordData->setTexVertexArray( texVertex, 4 );
      texCoordData->setTexFaceArray(texFaces,2);

        texUnit->setImageDataNum(1);
        //texUnit->setTextureCoordDataID(textureCoordDataID);
        texUnit->setTextureCoordData(texCoordData);
        texUnit->setWrapS(wrapS);
        texUnit->setWrapT(wrapT);
        texUnit->setImageDataID(imageIDArray,1);
        //texUnit->setImageData(0,imgd);

        SE_Vector3f color = SE_Vector3f(1,0,0);
             //->setColor(color);
       
        SE_Texture* texture = new SE_Texture;
        texture->setTextureUnit(0, texUnit);
        texture->setTexUnitNum(1);



        SE_Mesh* mesh = new SE_Mesh(1, 1);
        mesh->setGeometryData(geomData);
        mesh->setTexture(0, texture);

        SE_ProgramDataID ProgramDataID = "default_shader" ;
        SE_RendererID RendererID = "default_renderer";

        SE_Surface* surface = new SE_Surface;
        surface->setGeometryData(geomData);
        surface->setFacets(facet,2);
        //surface->setTexture(mesh->getTexture(0));//must
        surface->setTexture(texture);//must
        surface->setProgramDataID(ProgramDataID);
        surface->setRendererID(RendererID);
        //surface->setColor(color);
        mesh->setSurface(0, surface);
        SE_MeshSimObject* sim = new SE_MeshSimObject(mesh, OWN);
       
        SE_Spatial* root = SE_Application::getInstance()->getSceneManager()->getMainScene()->getRoot();
        SE_SpatialID spatialID = SE_ID::createSpatialID();
     

        SE_Geometry*  geometry = new SE_Geometry(spatialID, root);
        geometry->attachSimObject(sim);
        geometry->setBVType(AABB);
        geometry->setSpatialName("Xccc");
        SE_SimObjectID id = SE_ID::createSimObjectID();
        //SE_AABBBV* bv = new SE_AABBBV( SE_AABB(SE_Vector3f(-1,-1,-1) , SE_Vector3f(1,1,1) ));
        sim->setID(id);
        SE_Application::getInstance()->getSimObjectManager()->set(id, sim);
        geometry->updateWorldTransform();
       
        //geometry->setWorldBoundingVolume(bv);
        geometry->updateBoundingVolume();

        root->addChild(geometry);
        sim->setName("Xccc");
        SE_DepthTestState* rds = new SE_DepthTestState();
        rds->setDepthTestProperty(SE_DepthTestState::DEPTHTEST_DISABLE);
        //geometry->setRenderState(DEPTHTESTSTATE, rds);
       // geometry->updateRenderState();

        //SE_ImageDataID imageDataId = SE_ID::createImageDataID("D:\\flares.png");
        //SE_ImageDataID* imageIDArray = new SE_ImageDataID[1];
       // imageIDArray[0] = imageDataId;
        //mBillboardSet->getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageDataID(imageIDArray,1);
        //mBillboardSet->getMesh()->getSurface(0)->getTexture()->getTextureUnit(0)->setImageData(0,imgd);


        SE_BlendState *rs = new SE_BlendState();
        //rs->setBlendSrcFunc(SE_BlendState::SRC_ALPHA);
        //rs->setBlendDstFunc(SE_BlendState::ONE_MINUS_SRC_ALPHA);
        //rs->setBlendProperty(SE_BlendState::BLEND_ENABLE);
        //geometry->setRenderState(BLENDSTATE,rs);
        geometry->setLocalLayer(2);
        geometry->updateRenderState();
        geometry->updateWorldLayer();
          
        // World-relative axes
        //mBillboardSet->setBillboardsInWorldSpace(true);
        
#endif
        
                

                
#if 0

        SE_SceneManager* scm = SE_Application::getInstance()->getSceneManager();

        SE_Scene* main = scm->getMainScene();

        scm->removeScene(main);


        std::string datapath = "e:\\codebase\\win_svn_base\\assets\\base\\home";//"D:\\model\\jme\\home\\newhome3";
    std::string filename = "home";

        SE_InitAppCommand* c = (SE_InitAppCommand*)SE_Application::getInstance()->createCommand("SE_InitAppCommand");

#ifdef WIN32
    c->dataPath = datapath;
#else
    c->dataPath = "/home/luwei/model/jme/home/newhome3";
#endif
    c->fileName = filename;


    SE_Application::getInstance()->postCommand(c);

    SE_CreateSceneCommand *cs = (SE_CreateSceneCommand*)SE_Application::getInstance()->createCommand("SE_CreateSceneCommand");
    cs->dataPath = datapath;
    cs->fileName = filename;
    SE_Application::getInstance()->postCommand(cs);

    InitView();
#endif        


#if 0
        SE_PlayBipedAnimationCommand* c1 = (SE_PlayBipedAnimationCommand*)SE_Application::getInstance()->createCommand("SE_PlayBipedAnimationCommand");

        SE_Application::getInstance()->postCommand(c1);
#endif        
    }
    else if(PVRShellIsKeyPressed(PVRShellKeyNameDOWN))
    {
        //SE_SpatialManager* sm = SE_Application::getInstance()->getSpatialManager();
        /*SE_Spatial* a = SE_Application::getInstance()->getSceneManager()->getMainScene()->findSpatialByName("Box02@group_book@book_basedata.cbf");

        a->setTexCoordXYReverse(true,false);*/

#if 0
        SE_Vector3f a(0.9,0.9,0.9);
        SE_Vector3f b(0.9,0.9,0.9);
        float c = a.dot(b);
 
        SE_Vector3f boxsize;
        SE_Vector3f position = SE_Application::getInstance()->getCurrentCamera()->getLocation();

        ParticleSystemManager* particleSystemManager =  SE_Application::getInstance()->getParticleSystemManager();
        //particleSystemManager->removeEffect((SE_Effect)1);
        std::string particle = "test";
        std::string particleAttachedSpName = "Object09@group_house@home_basedata.cbf";
        //particleSystemManager->createParticleSystem(ParticleSystemName::Box, particle, particleAttachedSpName);            
        
        std::string path = "e:/codebase/win_svn_base/assets/base/sky/cloud_day_large_cloudy.png";
        std::string helppath = "e:/codebase/win_svn_base/assets/base/sky/cloud_day_large_cloudy.png";;
        SE_Vector2f d(200,200);
        //particleSystemManager->createEffect(SE_Effect::ATTACHED,particleAttachedSpName.c_str(),path.c_str(),helppath.c_str(),boxsize,d);
        particleSystemManager->createEffect(SE_Effect::STAR,position,path.c_str(),helppath.c_str(),boxsize);

        /*particleSystemManager->setImagePath(path.c_str(), "RAIN");
        particleSystemManager->setImagePath(helppath.c_str(), "HELPRAIN");*/
        LOGI("## down ##\n");
#endif

    }
}
bool SEDemo::RenderScene()
{
    int dwCurrentWidth = PVRShellGet (prefWidth);
    int dwCurrentHeight = PVRShellGet (prefHeight);
    handleInput(dwCurrentWidth, dwCurrentHeight);
    /*
    if(mPhysics)
    {
        mPhysics->stepSimulation(1.0f / 60);
        SE_Matrix4f m = mPhysics->getObjMatrix();
        SE_Vector3f v = m.getTranslate();
        if(groupNode)
        {
            groupNode->setLocalTranslate(v);
            groupNode->updateWorldTransform();
        }
        for(int i = 0 ; i < 4 ; i++)
        {
            SE_Vector4f v = m.getColumn(i);
            LOGI("## %d : %f %f %f %f\n", i, v.x, v.y, v.z, v.w);
        }
    }
    */
    SE_Application::getInstance()->run();
    int messageCount = SE_Application::getInstance()->getMessageCount();
    if(messageCount > 0)
    {
        //SE_Application::_MessageVector messageVector = SE_Application::getInstance()->getMessage();
        //for(int i = 0 ; i < messageVector.size() ; i++)
        //{
        //    SE_Message* msg = messageVector[i];
        //    //LOGI("### msg type = %d ####\n", msg->type);
        //    SE_Struct* structData = msg->data;
        //    int structItemSize = structData->getCount();
        //    //LOGI("### struct item size = %d ####\n", structItemSize);
        //    SE_StructItem* item = structData->getStructItem(0);
        //    SE_DataItem di = item->getDataItem(0);
        //    SE_StdString* strData = (SE_StdString*)di.data.virtualData;
        //    //LOGI("#### obj name = %s #### \n", strData->data.c_str());
        //}
        SE_Application::getInstance()->releaseMessage();
    }
    return true;
}
PVRShell* NewDemo()
{
    return new SEDemo();
}

