package io.bastillion.manage.auditing;

import io.bastillion.manage.auditing.rules.AuditorRule;
import io.bastillion.manage.auditing.rules.Trigger;
import io.bastillion.manage.auditing.rules.TriggerAction;
import io.bastillion.manage.db.JITProcessingDB;
import io.bastillion.manage.jit.JITReason;
import io.bastillion.manage.jit.JITRequest;
import io.bastillion.manage.model.Rule;
import io.bastillion.manage.model.SessionAudit;
import io.bastillion.manage.util.JITUtils;

import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RuleAlertAuditor extends BaseAuditor {

    public List<AuditorRule> synchronousRules = new ArrayList<>();

    public List<AuditorRule> asyncRules = new ArrayList<>();

    //private ConcurrentL

    final ExecutorService executorService;

    private AsyncRuleAuditorRunner runner;

    public RuleAlertAuditor(Long userId, Long sessionId, Long systemId) {

        super(userId, sessionId, systemId);
        // async thread evaluate
        executorService = Executors.newFixedThreadPool(1);

    }
    @Override
    protected void onPartial() {
        final String cstr = get();
        if (currentTrigger.getAction() == TriggerAction.DENY_ACTION){
            return;
        }
        System.out.println("Partial string is " + cstr);
        for(AuditorRule rule : synchronousRules){
            Optional<Trigger> result = rule.trigger(cstr);
            if (result.isPresent()){
                Trigger trg = result.get();
                switch(trg.getAction()){
                    case WARN_ACTION:
                        ShellAuditable.addWarning(trg);
                        break;
                    case JIT_ACTION:
                        ShellAuditable.addJIT(trg);
                        currentTrigger = trg;
                        System.out.println("Setting JIT");
                        break;
                    case DENY_ACTION:
                        ShellAuditable.addWarning(trg);
                        currentTrigger = trg;
                        break;
                    default:
                        break;
                }
            }
        }
        System.out.println("enqueueing " + cstr);
        runner.enqueue(cstr);
        // do nothing
    }

    public void setSynchronousRules(List<Rule> synchronousRules) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for(Rule rule : synchronousRules){
            Class<? extends AuditorRule> newRuleClass = Class.forName(rule.getRuleClass()).asSubclass(AuditorRule.class);
            AuditorRule newRule = newRuleClass.getConstructor().newInstance();
            newRule.configure(rule.getRuleConfig());
            switch( newRule.describeAction() ) {
                case JIT_ACTION:
                    this.synchronousRules.add(newRule);
                    this.asyncRules.add(newRule);
                    break;
                case DENY_ACTION:
                    System.out.println("going into SYNC rules");
                    this.synchronousRules.add(newRule);
                    break;
                default:
                    System.out.println("going into the async rules");
                    this.asyncRules.add(newRule);
            }

        }
        runner = new AsyncRuleAuditorRunner(asyncRules, userId, systemId);
        executorService.submit(runner);
    }


    @Override
    public void shutdown(){
        // nothing to do here
        executorService.shutdownNow();
    }

    private static final class AsyncRuleAuditorRunner implements Runnable{

        public AtomicBoolean running = new AtomicBoolean(false);

        final public List<AuditorRule> asyncRules;

        LinkedBlockingDeque<String> stringsToReview;

        Long userId;
        Long systemId;

        public AsyncRuleAuditorRunner(List<AuditorRule> asyncRules,
                                      Long userId,
                                      Long systemId){
            this.userId = userId;
            this.systemId = systemId;
            this.stringsToReview = new LinkedBlockingDeque<>();
            this.asyncRules=asyncRules;
            running.set(true);
        }

        public void enqueue(String cstr){
            stringsToReview.add(cstr);
        }

        @Override
        public void run() {
            while(running.get()) {
                try {


                    String nextstr = stringsToReview.poll(100, TimeUnit.MILLISECONDS);
                    if (null == nextstr)
                        continue;
                    System.out.println("nextstr is " + nextstr);
                    for (AuditorRule rule : asyncRules) {
                        Optional<Trigger> result = rule.trigger(nextstr);
                        if (result.isPresent()) {
                            Trigger trg = result.get();
                            switch (trg.getAction()) {
                                case WARN_ACTION:
                                    ShellAuditable.addWarning(trg);
                                    break;
                                case JIT_ACTION:
                                    if (!JITUtils.isApproved(nextstr,userId,systemId)){
                                        ShellAuditable.addJIT(trg);
                                    }
                                    System.out.println("Setting JIT 2");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } catch (InterruptedException | SQLException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("bye");
        }
    }

    @Override
    public synchronized String clear(int keycode){

        if (keycode == 13 && currentTrigger.getAction() == TriggerAction.DENY_ACTION) {
            System.out.println("no change");
        }
        else {
            currentTrigger = Trigger.NO_ACTION;
        }
        return super.clear(keycode);
    }

    @Override
    protected synchronized  TriggerAction submit(String command){
        //currentTrigger


        System.out.println("submit " + command + " " + currentTrigger.getAction());
        if (currentTrigger.getAction()== TriggerAction.JIT_ACTION) {
            // need to form a jit request
            try {
                if (!JITUtils.isApproved(command, userId, systemId)) {
                    System.out.println(command + " is not approved");
                    if (!JITProcessingDB.hasJITRequest(command, userId, systemId)) {
                        JITReason reason = JITUtils.createReason("need ", " ticket ", " url");
                        JITRequest request = JITUtils.createRequest(command, reason, userId, systemId);
                        request = JITProcessingDB.addJITRequest(request);
                        System.out.println("created " + request.getId());
                        
                    }

                    // keep the current trigger
                }
                else{
                    System.out.println(command + " is approved");
                    currentTrigger = Trigger.NO_ACTION;

                }

            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (Throwable t){
                t.printStackTrace();
                throw new RuntimeException(t.getMessage());
            }
            //JITProcessingDB.getJITStatus()

        }
        else{
            System.out.println("on message " + command);
        }
        return currentTrigger.getAction();
    }
}
