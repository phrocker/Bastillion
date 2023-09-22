package io.bastillion.manage.auditing.rules;

import java.util.Optional;

public class ForbiddenCommandsRule implements AuditorRule{

    //HashSet<>
    String command;
    TriggerAction action;
    int mode = 0;

    public ForbiddenCommandsRule(){
        action = TriggerAction.ALERT_ACTION;
    }
    String description;
    @Override
    public Optional<Trigger> trigger(String text) {
        switch(mode) {
            case 0:
                if (text.contains(this.command)){
                    return Optional.of(new Trigger(action, this.description));
                }
                break;
            case 1:
                if (text.endsWith(this.command)){
                    return Optional.of(new Trigger(action, this.description));
                }
                break;
            default:
                if (text.startsWith(this.command)){
                    return Optional.of(new Trigger(action, this.description));
                }

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
        else if (commandSplit.length == 4){

                this.command = commandSplit[0].trim();
                this.action = TriggerAction.valueOfStr(commandSplit[1].trim());
                this.description = commandSplit[2].trim();
                String where = commandSplit[3].trim();
                if (where.equals("any")) {
                    mode = 0;
                }
                else if (where.equals("end")) {
                    mode = 1;
                }
                else {
                    mode = 2;
                }
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
