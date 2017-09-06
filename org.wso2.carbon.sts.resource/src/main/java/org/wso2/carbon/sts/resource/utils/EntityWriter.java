package org.wso2.carbon.sts.resource.utils;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

public interface EntityWriter<T> {

    String CHUNKED = "chunked";

    /**
     * Provide supported entity type.
     *
     * @return entity type
     */
    Class<T> getType();

    /**
     * Write the entity object to the carbon message by considering the
     * provided chunk size and media type.
     *
     * @param carbonMessage response message
     * @param entity    object
     * @param mediaType user defined media type
     * @param chunkSize user defined chunk size
     *                  0 to signify none chunked response
     *                  -1 to signify default chunk size of the EntityWriter
     * @param cb        callback method that should be called to start sending the response payload
     */
    void writeData(CarbonMessage carbonMessage, T entity, String mediaType,
                   int chunkSize, CarbonCallback cb);

}