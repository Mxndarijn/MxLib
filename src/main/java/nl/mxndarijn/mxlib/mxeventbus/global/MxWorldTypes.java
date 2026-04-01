package nl.mxndarijn.mxlib.mxeventbus.global;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts a {@link MxGlobalEventBus} handler method to events that originate from
 * one or more specific {@link MxWorldType} categories.
 *
 * <p>When absent, the handler runs for events from <em>all</em> world types.
 * When present with an empty array, the handler is effectively disabled.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Subscribe
 * @WorldTypes({WorldType.SPAWN, MxWorldType.PRESET})
 * public void onChat(MxGlobalEventContext<MxGlobalChatEvent, MxWorldType> ctx) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MxWorldTypes {

    /**
     * The world type categories in which this handler should run.
     *
     * @return array of permitted {@link MxWorldType} values; empty means the handler never runs
     */
    MxWorldType[] value();
}



