package uk.co.eluinhost.ultrahardcore.commands.teststructure;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.lang.reflect.Method;
import java.util.*;

public class BukkitCommandHandler implements TabExecutor {

    /**
     * Stores a list of class
     */
    private final Map<String,Object> m_instances = new HashMap<String,Object>();

    private final Collection<VirtualCommand> m_commands = new LinkedList<VirtualCommand>();

    private static final String COMMAND_NOT_FOUND = ChatColor.RED + "Couldn't find the command requested";
    private static final String COMMAND_SENDER_NOT_ALLOWED = ChatColor.RED + "You can't run that command from here";
    private static final String COMMAND_EXCEPTION = ChatColor.RED + "Internal Server Error when running command, please check console for more information";

    private static final class BukkitCommandHandlerHolder {
        private static final BukkitCommandHandler COMMAND_HANDLER = new BukkitCommandHandler();
    }

    /**
     * @return instance of the bukkit command handler
     */
    public static final BukkitCommandHandler getInstance(){
        return BukkitCommandHandlerHolder.COMMAND_HANDLER;
    }

    /**
     * Create the bukkit command handler
     */
    private BukkitCommandHandler() {}

    /**
     * Register the commands within the class
     * @param clazz the class to check
     */
    public void registerCommands(Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for(Method method : methods){
            Command methodAnnotation = method.getAnnotation(Command.class);
            if(methodAnnotation != null){
                //noinspection OverlyBroadCatchBlock
                try {
                    addCommand(clazz, method,methodAnnotation);
                } catch (Exception ignored) {
                    Bukkit.getLogger().severe("Error trying to process the command method "+method.getName()+" in the class "+clazz.getName());
                }
            }
        }
    }

    /**
     * Gets the instance stored for the class name
     * @param className the class name to check for
     * @return the class if exists or null otherwise
     */
    public Object getClassInstance(String className) {
        return m_instances.get(className);
    }

    /**
     * Add the command to the command map, must be a valid class/method/annotation combo before calling
     * @param clazz The class to store it under
     * @param method the method to invoke
     * @param annotation the annotation to get details from
     * @throws CommandIDConflictException when the command ID is already taken
     * @throws CommandCreateException when the command class couldn't be created for use
     * @throws CommandParentNotFoundException when the command parent couldn't be found in the tree
     */
    private void addCommand(Class clazz,Method method,Command annotation) throws CommandIDConflictException, CommandParentNotFoundException, CommandCreateException {
        String commandID = annotation.id();
        String parentID = annotation.parentID();

        if(getVirtualCommandByID(commandID) != null){
            throw new CommandIDConflictException();
        }
        Object storedInstance = getClassInstance(clazz.getName());
        if(null == storedInstance){
            //noinspection OverlyBroadCatchBlock
            try {
                storedInstance = clazz.getConstructor().newInstance();
                m_instances.put(clazz.getName(),storedInstance);
            } catch (Exception ignored) {
                throw new CommandCreateException();
            }
        }
        VirtualCommand command = new VirtualCommand(method,annotation,clazz.getName());
        if(parentID.isEmpty()){
            m_commands.add(command);
        } else {
            VirtualCommand parentCommand = getVirtualCommandByID(parentID);
            if(null == parentCommand){
                throw new CommandParentNotFoundException();
            }
            parentCommand.addChild(command);
        }
    }

    /**
     * Gets the command by its ID, searches entire tree
     * @param id the command's ID
     * @return the virtualcommand if found, null otherwise
     */
    public VirtualCommand getVirtualCommandByID(String id){
        for(VirtualCommand command : m_commands){
            if(command.getByID(id) != null){
                return command;
            }
        }
        return null;
    }

    /**
     * @param name the name to look for, case insensitive
     * @return the root command with the name
     */
    public VirtualCommand getRootCommandByName(String name){
        for(VirtualCommand command : m_commands){
            if(command.getCommand().name().equalsIgnoreCase(name)){
                return command;
            }
        }
        return null;
    }

    /**
     * Converts arguements with the " char to use 1 index
     * @param args the arguements to parse
     * @return array of converted strings
     */
    private static List<String> convertArgs(String[] args){
        List<String> finalArgs = new ArrayList<String>();
        for(int i = 0; i < args.length; i++){
            String arg = args[i];
            if(arg.charAt(0) == '"'){
                StringBuilder build = new StringBuilder();
                build.append(arg.substring(1));
                for(i += 1;i<args.length;i++){
                    build.append(" ");
                    String quotedArg = args[i];
                    if(quotedArg.charAt(quotedArg.length()-1) == '"'){
                        build.append(quotedArg.substring(0,quotedArg.length()-1));
                        break;
                    }else{
                        build.append(quotedArg);
                    }
                }
                finalArgs.add(build.toString());
            }else{
                finalArgs.add(arg);
            }
        }
        return finalArgs;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        //get the root command
        VirtualCommand virtualCommand = getRootCommandByName(command.getName());
        if(null == virtualCommand){
            sender.sendMessage(COMMAND_NOT_FOUND);
            return true;
        }
        //for each of the arguements check for nested commands
        List<String> convertedArgs = convertArgs(args);

        Iterator<String> arguementIterator = convertedArgs.iterator();
        while(arguementIterator.hasNext()){
            String arg = arguementIterator.next();
            //if the command has the subcommand try and check against that
            //otherwise use the current selected command
            VirtualCommand subCommand = virtualCommand.getChild(arg);
            if(subCommand != null){
                arguementIterator.remove();
                virtualCommand = subCommand;
            }else{
                break;
            }
        }
        if(!virtualCommand.isSenderAllowed(SenderType.getFromCommandSender(sender))){
            sender.sendMessage(COMMAND_SENDER_NOT_ALLOWED);
            return true;
        }

        CommandRequest request = new CommandRequest();
        //noinspection OverlyBroadCatchBlock
        try {
            virtualCommand.runCommand(request);
        } catch (Exception e) {
            sender.sendMessage(COMMAND_EXCEPTION);
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        //TODO ... or not
        return null;
    }
}