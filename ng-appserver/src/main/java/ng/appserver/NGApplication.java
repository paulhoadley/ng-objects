package ng.appserver;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ng.appserver.privates.NGParsedURI;

public class NGApplication {

	private static Logger logger = LoggerFactory.getLogger( NGApplication.class );

	private static NGApplication _application;

	private NGSessionStore _sessionStore;

	private NGResourceManager _resourceManager;

	/**
	 * FIXME: Needs to be thread safe?
	 */
	private Map<String, NGRequestHandler> _requestHandlers = new HashMap<>();

	/**
	 * FIXME: Not sure if this method should actually be provided 
	 */
	public static void main( final String[] args ) {
		main( args, NGApplication.class );
	}

	public static void main( final String[] args, final Class<? extends NGApplication> applicationClass ) {
		try {
			_application = applicationClass.getDeclaredConstructor().newInstance();
			
			_application._resourceManager = new NGResourceManager();
			_application._sessionStore = new NGServerSessionStore();

			_application.registerRequestHandler( "wo", new NGComponentRequestHandler() );
			_application.registerRequestHandler( "wr", new NGResourceRequestHandler() );
			_application.registerRequestHandler( "wa", new NGDirectActionRequestHandler() );

			_application.run();
		}
		catch( Exception e ) {
			e.printStackTrace();
			System.exit( -1 );
		}
	}

	public NGComponent pageWithName( final Class<? extends NGComponent> componentClass, NGContext context ) {
		try {
			return componentClass.getConstructor( NGContext.class ).newInstance( context );
		}
		catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e ) {
			// FIXME: Handle the error
			throw new RuntimeException( e );
		}
	}

	public NGSessionStore sessionStore() {
		return _sessionStore;
	}

	public NGSession restoreSessionWithID( final String sessionID ) {
		return sessionStore().checkoutSessionWithID( sessionID );
	}

	private NGAdaptor createAdaptor() {
		try {
			final Class<? extends NGAdaptor> adaptorClass = (Class<? extends NGAdaptor>)Class.forName( adaptorClassName() );
			return adaptorClass.getConstructor().newInstance();
		}
		catch( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e ) {
			// FIXME: Handle the error
			e.printStackTrace();
			System.exit( -1 );
			return null;  // wat?
		}
	}
	
	/**
	 * FIXME: We don't really want to return anything if this hasn't been set. Only set now for testing 
	 */
	public String adaptorClassName() {
		return "ng.adaptor.jetty.NGAdaptorJetty";
	}

	private void run() {
		try {
			createAdaptor().start();
		}
		catch( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	public static NGApplication application() {
		return _application;
	}

	public NGResourceManager resourceManager() {
		return _resourceManager;
	}

	public void registerRequestHandler( final String key, final NGRequestHandler requestHandler ) {
		_requestHandlers.put( key, requestHandler );
	}

	public NGResponse dispatchRequest( final NGRequest request ) {

		logger.info( "Handling URI: " + request.uri() );

		// FIXME: Handle the case of no default request handler gracefully
		final var parsedURI = NGParsedURI.of( request.uri() );

		final Optional<String> requestHandlerKey = parsedURI.elementAt( 0 );

		if( requestHandlerKey.isEmpty() ) {
			return new NGResponse( "I have no idea to handle requests without any path elements", 404 );
		}

		final NGRequestHandler requestHandler = _requestHandlers.get( requestHandlerKey.get() );

		if( requestHandler == null ) {
			return new NGResponse( "No request handler found with key " + requestHandlerKey, 404 );
		}

		return requestHandler.handleRequest( request );
	}

	public NGContext createContextForRequest( NGRequest request ) {
		return new NGContext( request );
	}
}