package nl.mxndarijn.mxlib.mxeventbus.core;

import java.util.List;

/**
 * Built-in strategies for resolving a list of handler-submitted {@link MxCancellationState}
 * verdicts into a single final verdict, evaluated before MONITOR-priority handlers run.
 *
 * <p>Events declare which strategy to use via {@link MxDefaultCancellation#resolver()}.
 * Each constant implements the {@link #resolve(List, MxCancellationState)} method.</p>
 */
public enum MxVerdictResolver {

    /**
     * Any {@link MxCancellationState#HARD_DENY} wins over everything; then
     * {@link MxCancellationState#HARD_ALLOW}; then {@link MxCancellationState#SOFT_DENY};
     * then {@link MxCancellationState#SOFT_ALLOW}; otherwise {@link MxCancellationState#PASS}.
     *
     * <p>Recommended for events that must be blockable (e.g. crafting, movement).</p>
     */
    HARD_DENY_WINS {
        @Override
        public MxCancellationState resolve(List<MxCancellationState> verdicts, MxCancellationState defaultState) {
            if (verdicts.isEmpty()) return defaultState;
            if (verdicts.contains(MxCancellationState.HARD_DENY))  return MxCancellationState.HARD_DENY;
            if (verdicts.contains(MxCancellationState.HARD_ALLOW)) return MxCancellationState.HARD_ALLOW;
            if (verdicts.contains(MxCancellationState.SOFT_DENY))  return MxCancellationState.SOFT_DENY;
            if (verdicts.contains(MxCancellationState.SOFT_ALLOW)) return MxCancellationState.SOFT_ALLOW;
            return MxCancellationState.PASS;
        }
    },

    /**
     * Any {@link MxCancellationState#HARD_ALLOW} wins over everything; then
     * {@link MxCancellationState#HARD_DENY}; then {@link MxCancellationState#SOFT_ALLOW};
     * then {@link MxCancellationState#SOFT_DENY}; otherwise {@link MxCancellationState#PASS}.
     *
     * <p>Use for events that should be permitted unless explicitly hard-denied.</p>
     */
    HARD_ALLOW_WINS {
        @Override
        public MxCancellationState resolve(List<MxCancellationState> verdicts, MxCancellationState defaultState) {
            if (verdicts.isEmpty()) return defaultState;
            if (verdicts.contains(MxCancellationState.HARD_ALLOW)) return MxCancellationState.HARD_ALLOW;
            if (verdicts.contains(MxCancellationState.HARD_DENY))  return MxCancellationState.HARD_DENY;
            if (verdicts.contains(MxCancellationState.SOFT_ALLOW)) return MxCancellationState.SOFT_ALLOW;
            if (verdicts.contains(MxCancellationState.SOFT_DENY))  return MxCancellationState.SOFT_DENY;
            return MxCancellationState.PASS;
        }
    },

    /**
     * The last verdict submitted by any handler wins.
     * If no verdicts were submitted, returns the event's default state.
     */
    LAST_WINS {
        @Override
        public MxCancellationState resolve(List<MxCancellationState> verdicts, MxCancellationState defaultState) {
            return verdicts.isEmpty() ? defaultState : verdicts.get(verdicts.size() - 1);
        }
    },

    /**
     * The first verdict submitted by any handler wins.
     * If no verdicts were submitted, returns the event's default state.
     */
    FIRST_WINS {
        @Override
        public MxCancellationState resolve(List<MxCancellationState> verdicts, MxCancellationState defaultState) {
            return verdicts.isEmpty() ? defaultState : verdicts.get(0);
        }
    };

    /**
     * Resolves a list of handler-submitted verdicts into one final {@link MxCancellationState}.
     *
     * @param verdicts     ordered list of verdicts submitted by handlers; never {@code null}, may be empty
     * @param defaultState the event's declared default state, used as fallback when the list is empty
     * @return the resolved final state; never {@code null}
     */
    public abstract MxCancellationState resolve(List<MxCancellationState> verdicts, MxCancellationState defaultState);
}


