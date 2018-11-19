package io.rapidpro.surveyor.utils;

import com.nyaruka.goflow.mobile.AssetsSource;
import com.nyaruka.goflow.mobile.Contact;
import com.nyaruka.goflow.mobile.Environment;
import com.nyaruka.goflow.mobile.Event;
import com.nyaruka.goflow.mobile.EventSlice;
import com.nyaruka.goflow.mobile.FlowReference;
import com.nyaruka.goflow.mobile.Mobile;
import com.nyaruka.goflow.mobile.MsgIn;
import com.nyaruka.goflow.mobile.Resume;
import com.nyaruka.goflow.mobile.Session;
import com.nyaruka.goflow.mobile.SessionAssets;
import com.nyaruka.goflow.mobile.StringSlice;
import com.nyaruka.goflow.mobile.Trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.rapidpro.surveyor.data.Org;

/**
 * Wraps functionality in the goflow mobile library module
 */
public class Engine {
    /**
     * Migrates a legacy flow definition to the new engine format
     *
     * @param definition the legacy definition
     * @return the new definition
     */
    public static String migrateFlow(String definition) {
        try {
            return Mobile.migrateLegacyFlow(definition);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets whether the given flow spec version is supported by the flow engine
     *
     * @param ver the spec version
     * @return true if supported
     */
    public static boolean isSpecVersionSupported(String ver) {
        return Mobile.isSpecVersionSupported(ver);
    }

    /**
     * Creates an engine environment from the given org
     *
     * @param org the org
     * @return the environment
     */
    public static Environment createEnvironment(Org org) {
        String dateFormat = org.getDateStyle().equals("day_first") ? "DD-MM-YYYY" : "MM-DD-YYYY";
        String timeformat = "tt:mm";
        StringSlice languages = listToSlice(Arrays.asList(org.getLanguages()));
        String redactionPolicy = org.isAnon() ? "urns" : "none";
        return new Environment(dateFormat, timeformat, org.getTimezone(), org.getPrimaryLanguage(), languages, redactionPolicy);
    }

    /**
     * Creates a new empty contact
     *
     * @return the contact
     */
    public static Contact createEmptyContact() {
        return Mobile.newEmptyContact();
    }

    /**
     * Loads an assets source from the given JSON
     *
     * @param json the assets JSON
     * @return the source
     */
    public static AssetsSource loadAssets(String json) throws EngineException {
        try {
            return new AssetsSource(json);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    /**
     * Creates a new session assets instance from an assets source
     *
     * @param source the source
     * @return the session assets
     */
    public static SessionAssets createSessionAssets(AssetsSource source) throws EngineException {
        try {
            return new SessionAssets(source);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    /**
     * Creates a new session
     *
     * @param assets the session assets
     * @return the session
     */
    public static Session createSession(SessionAssets assets) throws EngineException {
        try {
            return new Session(assets, null);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    /**
     * Starts a session with a trigger
     *
     * @param session the session
     * @param trigger the trigger
     * @return the events
     */
    public static List<Event> startSession(Session session, Trigger trigger) throws EngineException {
        try {
            return eventSliceToList(session.start(trigger));
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    /**
     * Resumes a session with a resume
     *
     * @param session the session
     * @param resume the resume
     * @return the events
     */
    public static List<Event> resumeSession(Session session, Resume resume) throws EngineException {
        try {
            return eventSliceToList(session.resume(resume));
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    /**
     * Creates a new flow reference
     *
     * @param uuid the flow UUID
     * @param name the flow name
     * @return the reference
     */
    public static FlowReference createFlowReference(String uuid, String name) {
        return new FlowReference(uuid, name);
    }

    /**
     * Creates a new incoming message
     *
     * @param uuid the message UUID
     * @param text the message text
     * @param attachments the message attachments
     * @return the message
     */
    public static MsgIn createMsgIn(String uuid, String text, List<String> attachments) {
        return new MsgIn(uuid, text, listToSlice(attachments));
    }

    /**
     * Creates a new manual trigger
     *
     * @param env the environment
     * @param contact the contact
     * @param flow the flow reference
     * @return the trigger
     */
    public static Trigger createManualTrigger(Environment env, Contact contact, FlowReference flow) {
        return Mobile.newManualTrigger(env, contact, flow);
    }

    /**
     * Creates a new message resume
     *
     * @param env the environment
     * @param contact the contact
     * @param msg the message
     * @return the resume
     */
    public static Resume createMsgResume(Environment env, Contact contact, MsgIn msg) {
        return Mobile.newMsgResume(env, contact, msg);
    }

    private static StringSlice listToSlice(List<String> items) {
        if (items == null) {
            return null;
        }
        StringSlice slice = new StringSlice(items.size());
        for (String item : items) {
            slice.add(item);
        }
        return slice;
    }

    private static List<Event> eventSliceToList(EventSlice slice) {
        List<Event> events = new ArrayList<>((int)slice.length());
        for (int e = 0; e < slice.length(); e++) {
            events.add(slice.get(e));
        }
        return events;
    }
}
