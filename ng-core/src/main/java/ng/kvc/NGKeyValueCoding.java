package ng.kvc;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * NGKeyValueCoding is a simplified reimplementation of NSKeyValueCoding.
 *
 * FIXME: Currently only handles
 * FIXME: Implement missing key handling
 * FIXME: Decide if we're going to go all the way and do validation as well
 * FIXME: We should be marking bindings for directionality (read only/set only etc)
 */

public interface NGKeyValueCoding {

	public Object valueForKey( String key );

	public void takeValueForKey( Object value, String key );

	public static class Utility {

		public static Object valueForKey( final Object object, final String key ) {
			Objects.requireNonNull( object );
			Objects.requireNonNull( key );
			return bindingForKey( object.getClass(), key ).valueInObject( object );
		}
	}

	/**
	 * @return A KVC binding for the given class and key.
	 */
	public static KVCBinding bindingForKey( Class<?> targetClass, String key ) {
		MethodBinding m = new MethodBinding();
		m.targetClass = targetClass;
		m.key = key;
		return m;
	}

	public static interface KVCBinding {
		public Object valueInObject( Object object );
	}

	public static class MethodBinding implements KVCBinding {

		public String key;
		public Class<?> targetClass;

		@Override
		public Object valueInObject( Object object ) {
			try {
				return targetClass.getMethod( key, new Class[] {} ).invoke( object, null );
			}
			catch( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				// FIXME: Error handling is missing entirely
				throw new RuntimeException( e );
			}
		}
	}

	public static class FieldBinding implements KVCBinding {

		@Override
		public Object valueInObject( Object object ) {
			throw new RuntimeException( "Not Implemented" );
		}
	}
}