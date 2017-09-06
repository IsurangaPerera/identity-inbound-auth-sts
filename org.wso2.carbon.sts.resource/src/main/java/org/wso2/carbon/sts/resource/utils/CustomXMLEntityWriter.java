package org.wso2.carbon.sts.resource.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.beanconversion.BeanConversionException;

public class CustomXMLEntityWriter implements EntityWriter<String> {

	/**
	 * Supported entity type.
	 */
	@Override
	public Class<String> getType() {
		return String.class;
	}

	/**
	 * Write the entity to the carbon message.
	 */
	@Override
	public void writeData(CarbonMessage carbonMessage, String entity,
			String mediaType, int chunkSize, CarbonCallback cb) {
		ByteBuffer byteBuffer = convertToMedia(entity);
		carbonMessage.addMessageBody(byteBuffer);
		carbonMessage.setEndOfMsgAdded(true);
		if (chunkSize == Response.NO_CHUNK) {
			carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH,
					String.valueOf(byteBuffer.remaining()));
		} else {
			carbonMessage.setHeader(Constants.HTTP_TRANSFER_ENCODING, CHUNKED);
		}
		carbonMessage.setHeader(Constants.HTTP_CONTENT_TYPE, MediaType.TEXT_XML);
		cb.done(carbonMessage);
	}
	
	private ByteBuffer convertToMedia(Object object) {
        if (object == null) {
            throw new BeanConversionException("Object cannot be null");
        }
        return toMedia(object);
    }
	
	private ByteBuffer toMedia(Object object) {
        return ByteBuffer.wrap(object.toString().getBytes(Charset.defaultCharset()));
    }
}