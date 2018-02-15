package org.moskito.javaagent.request.journey;

import net.anotheria.moskito.core.config.MoskitoConfigurationHolder;
import net.anotheria.moskito.core.config.tagging.CustomTag;
import net.anotheria.moskito.core.config.tagging.CustomTagSource;
import net.anotheria.moskito.core.config.tagging.TaggingConfig;
import net.anotheria.moskito.core.context.MoSKitoContext;
import net.anotheria.moskito.core.tag.TagType;
import org.moskito.javaagent.request.RequestListener;
import org.moskito.javaagent.request.RequestResultData;
import org.moskito.javaagent.request.config.RequestListenerConfiguration;
import org.moskito.javaagent.request.wrappers.HttpRequestWrapper;
import org.moskito.javaagent.request.wrappers.HttpSessionWrapper;

public class TagsListener implements RequestListener {

    private static final String TAG_IP = "ip";
    private static final String TAG_REFERER = "referer";
    private static final String TAG_USER_AGENT = "user-agent";
    private static final String TAG_SESSION_ID = "sessionId";

    @Override
    public void onRequestStarted(HttpRequestWrapper request) {

        HttpSessionWrapper session = request.getSession(false);

        TaggingConfig taggingConfig = MoskitoConfigurationHolder.getConfiguration().getTaggingConfig();
        if (taggingConfig.isAutotagIp()) {
            MoSKitoContext.addTag(TAG_IP, request.getRemoteAddr(), TagType.BUILTIN, TagType.BUILTIN.getName() + '.' + TAG_IP);
        }
        if (taggingConfig.isAutotagReferer()) {
            MoSKitoContext.addTag(TAG_REFERER, request.getHeader(TAG_REFERER), TagType.BUILTIN, TagType.BUILTIN.getName() + '.' + TAG_REFERER);
        }
        if (taggingConfig.isAutotagUserAgent()) {
            MoSKitoContext.addTag(TAG_USER_AGENT, request.getHeader(TAG_USER_AGENT), TagType.BUILTIN, TagType.BUILTIN.getName() + '.' + TAG_USER_AGENT);
        }
        if (taggingConfig.isAutotagSessionId() && session != null) {
            MoSKitoContext.addTag(TAG_SESSION_ID, session.getId(), TagType.BUILTIN, TagType.BUILTIN.getName() + '.' + TAG_SESSION_ID);
        }

        //set custom tags
        for (CustomTag tag : taggingConfig.getCustomTags()) {
            if (CustomTagSource.HEADER.getName().equals(tag.getAttributeSource())) {
                MoSKitoContext.addTag(tag.getName(), request.getHeader(tag.getAttributeName()), TagType.CONFIGURED, tag.getAttribute());
            } else if (CustomTagSource.REQUEST.getName().equals(tag.getAttributeSource())) {
                MoSKitoContext.addTag(tag.getName(), (String) request.getAttribute(tag.getAttributeName()), TagType.CONFIGURED, tag.getAttribute());
            } else if (CustomTagSource.SESSION.getName().equals(tag.getAttributeSource()) && session != null) {
                MoSKitoContext.addTag(tag.getName(), (String) session.getAttribute(tag.getAttributeName()), TagType.CONFIGURED, tag.getAttribute());
            } else if (CustomTagSource.PARAMETER.getName().equals(tag.getAttributeSource())) {
                MoSKitoContext.addTag(tag.getName(), request.getParameter(tag.getAttributeName()), TagType.CONFIGURED, tag.getAttribute());
            }
        }

    }

    @Override
    public void onRequestFinished(HttpRequestWrapper request, RequestResultData resultData) {

    }

    @Override
    public void configure(RequestListenerConfiguration conf) {

    }

}
