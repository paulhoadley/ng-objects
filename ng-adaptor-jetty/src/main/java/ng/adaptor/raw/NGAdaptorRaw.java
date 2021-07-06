package ng.adaptor.raw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ng.appserver.NGAdaptor;
import ng.appserver.NGRequest;
import ng.appserver.NGResponse;

/**
 * Experimental raw socket adaptor
 */

public class NGAdaptorRaw extends NGAdaptor {

	private static final String CRLF = "\r\n";
	private static Logger logger = LoggerFactory.getLogger( NGAdaptorRaw.class );

	@Override
	public void start() {
		final int port = 1200;

		try( final ServerSocket serverSocket = new ServerSocket( port ) ;) {
			logger.info( "Started listening for connections on port {}", port );
			while( true ) {
				final Socket clientSocket = serverSocket.accept();

				final InputStream inStream = clientSocket.getInputStream();
				final OutputStream outStream = clientSocket.getOutputStream();

				final NGRequest request = requestFromInputStream( inStream );
				final NGResponse response = dispatchRequest( request );

				writeResponseToStream( response, outStream );

				inStream.close();
				outStream.close();
				clientSocket.close();
			}
		}
		catch( final Exception e ) {
			// FIXME: Actually handle this exception
			e.printStackTrace();
		}
	}

	/**
	 * FIXME: We're currently writing directly to the stream. Some sort of buffered writing would probably be more appropriate
	 */
	private static void writeResponseToStream( final NGResponse response, final OutputStream stream ) {
		try {
			write( stream, response.httpVersion() + " " + response.status() + " OK" ); // FIXME: Don't just write OK here!
			write( stream, CRLF ); // FIXME: Do we need CRLF?

			// FIXME: Currently only writing out the first header value
			for( final Entry<String, List<String>> entry : response.headers().entrySet() ) {
				write( stream, entry.getKey() );
				write( stream, ": " );
				write( stream, entry.getValue().get( 0 ) );
				write( stream, CRLF );
			}

			write( stream, CRLF );
			stream.write( response.contentBytes() );
		}
		catch( final IOException e ) {
			// FIXME: Actually handle this exception
			e.printStackTrace();
		}
	}

	private static void write( OutputStream stream, final String string ) {
		try {
			stream.write( string.getBytes( StandardCharsets.UTF_8 ) );
		}
		catch( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @returns An NGRequest parsed from the InputStream
	 */
	private static NGRequest requestFromInputStream( final InputStream stream ) {
		try {
			final BufferedReader in = new BufferedReader( new InputStreamReader( stream ) );

			String method = null;
			String uri = null;
			String httpVersion = null;
			final Map<String, List<String>> headers = new HashMap<>();
			final byte[] content = null;
			String line;

			int currentLineNumber = 0;

			while( (line = in.readLine()) != null ) {
				logger.info( "Parsing request line {} : {}", currentLineNumber, line );

				if( currentLineNumber++ == 0 ) {
					final String[] values = line.split( " " );
					method = values[0];
					uri = values[1];
					httpVersion = values[2];
				}
				else {
					final int colonIndex = line.indexOf( ":" );

					// FIXME: Handle multiple same-name headers
					// FIXME: Handle multiple header values
					if( colonIndex > -1 ) {
						final String headerName = line.substring( 0, colonIndex );
						final String headerValueString = line.substring( colonIndex ).trim(); // FIXME: Not sure if trimming is the right thing to do here

						headers.put( headerName, Arrays.asList( headerValueString ) );
					}
				}

				// FIXME: Handle the request content
				// An empty line should denote the end of the header section and the beginning of the content section
				if( line.isEmpty() ) {
					break;
				}
			}

			return new NGRequest( method, uri, httpVersion, headers, content );
		}
		catch( final IOException e ) {
			throw new RuntimeException( e );
		}
	}
}