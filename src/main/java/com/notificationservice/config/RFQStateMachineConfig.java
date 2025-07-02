package com.notificationservice.config;

import com.notificationservice.entity.RFQ;
import com.notificationservice.entity.RFQEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Configuration
@EnableStateMachine(name = "rfqStateMachine")
public class RFQStateMachineConfig extends EnumStateMachineConfigurerAdapter<RFQ.State, RFQEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<RFQ.State, RFQEvent> states) throws Exception {
        states
                .withStates()
                .initial(RFQ.State.DRAFT)
                .states(EnumSet.allOf(RFQ.State.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<RFQ.State, RFQEvent> transitions) throws Exception {
        transitions
                .withExternal().source(RFQ.State.DRAFT).target(RFQ.State.ENRICHMENT_PENDING)
                .event(RFQEvent.SUBMIT_FOR_ENRICHMENT)
                .and()
                .withExternal().source(RFQ.State.ENRICHMENT_PENDING).target(RFQ.State.ENRICHMENT_COMPLETED)
                .event(RFQEvent.ENRICHMENT_COMPLETE)
                .and()
                .withExternal().source(RFQ.State.ENRICHMENT_PENDING).target(RFQ.State.CANCELLED).event(RFQEvent.CANCEL)
                .and()
                .withExternal().source(RFQ.State.ENRICHMENT_COMPLETED).target(RFQ.State.CLOSED).event(RFQEvent.CLOSE)
                .and()
                .withExternal().source(RFQ.State.DRAFT).target(RFQ.State.CANCELLED).event(RFQEvent.CANCEL)
                .and()
                .withExternal().source(RFQ.State.ENRICHMENT_COMPLETED).target(RFQ.State.CANCELLED)
                .event(RFQEvent.CANCEL);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<RFQ.State, RFQEvent> config) throws Exception {
        config
                .withConfiguration()
                .listener(new StateMachineListenerAdapter<RFQ.State, RFQEvent>() {
                    @Override
                    public void stateChanged(State<RFQ.State, RFQEvent> from, State<RFQ.State, RFQEvent> to) {
                        // Optionally log or handle state changes
                    }
                });
    }
}