package ng.kvc;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestNGKeyValueCoding {

	@Test
	public void testMethodAccess() {
		Person person = new Person( "Hugi" );

		assertEquals( "interfaceValue", NGKeyValueCoding.Utility.valueForKey( person, "getClass" ) );
		assertEquals( "Hugi", NGKeyValueCoding.DefaultImplementation.valueForKey( person, "name" ) );
		assertEquals( Person.class, NGKeyValueCoding.DefaultImplementation.valueForKey( person, "getClass" ) );
		assertNull( NGKeyValueCoding.DefaultImplementation.valueForKey( person, "returnsNull" ) );
	}

	@Test
	public void testFieldAccess() {
		Home home = new Home();
		home.address1 = "Hraunteigur 23";

		assertEquals( "Hraunteigur 23", NGKeyValueCoding.DefaultImplementation.valueForKey( home, "address1" ) );
		assertNull( NGKeyValueCoding.DefaultImplementation.valueForKey( home, "address2" ) );
	}

	public record Person( String name ) implements NGKeyValueCoding {

		@Override
		public Object valueForKey( String key ) {
			return "interfaceValue";
		}

		public Object returnsNull() {
			return null;
		}

		@Override
		public void takeValueForKey( Object value, String key ) {
			// FIXME: Implement
		}
	}

	public static class Home {
		public String address1;
		public String address2;
	}
}