package io.bastillion.manage.auditing.rules;

import java.util.Optional;

public class ForbiddenCommandsRule implements AuditorRule{

    //HashSet<>
    String command;
    TriggerAction action;

    public ForbiddenCommandsRule(){
        action = TriggerAction.ALERT_ACTION;
    }
    String description;
    @Override
    public Optional<Trigger> trigger(String text) {
        if (text.startsWith(this.command)){
            return Optional.of(new Trigger(action, this.description));
        }


        return Optional.empty();
    }

    @Override
    public boolean configure(String configuration) {

        System.out.println("got configuration " + configuration);
        String [] commandSplit = configuration.split(":");


        if (commandSplit.length == 3){

            this.command = commandSplit[0].trim();
            this.action = TriggerAction.valueOfStr(commandSplit[1].trim());
            this.description = commandSplit[2].trim();
            System.out.println("got configuration " + command);
            return true;
        }
        return false;
    }

    @Override
    public TriggerAction describeAction() {
        return action;
    }
}
