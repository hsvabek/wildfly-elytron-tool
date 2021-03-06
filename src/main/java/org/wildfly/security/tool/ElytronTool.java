/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.security.tool;

import org.wildfly.security.WildFlyElytronProvider;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

/**
 * Elytron Tool main class which drives all registered commands.
 *
 * @author <a href="mailto:pskopek@redhat.com">Peter Skopek</a>
 */
public class ElytronTool {

    /**
     * status code for unrecognized command
     */
    public static int ElytronToolExitStatus_unrecognizedCommand = 1;
    /**
     * status code for no problems
     */
    public static int ElytronToolExitStatus_OK = 0;

    /**
     *
     */
    public static final String TOOL_JAR = "wildfly-elytron-tool.jar";

    private Map<String, Command> commandRegistry = new HashMap<>();


    /**
     * Construct ElytronTool with registration of all supported commands.
     */
    public ElytronTool() {
        commandRegistry.put(CredentialStoreCommand.CREDENTIAL_STORE_COMMAND, new CredentialStoreCommand()); // assigned exit codes 5 - 10
        commandRegistry.put(MaskCommand.MASK_COMMAND, new MaskCommand()); // uses exit code 7
        commandRegistry.put(VaultCommand.VAULT_COMMAND, new VaultCommand()); // uses exit code 7
    }

    /**
     * Main method to call from scripts.
     *
     * @param args parameters to pass farther. The first parameter is name or alias of the command.
     */
    public static void main(String[] args) {

        Security.addProvider(new WildFlyElytronProvider());

        ElytronTool tool = new ElytronTool();
        if (args != null && args.length > 0) {
            Command command = tool.findCommand(args[0]);
            if (command != null) {
                String[] newArgs = new String[args.length -1];
                System.arraycopy(args, 1, newArgs, 0, args.length -1);
                try {
                    command.execute(newArgs);
                    System.exit(command.getStatus());
                } catch (Exception e) {
                    System.err.println(ElytronToolMessages.msg.commandExecuteException());
                    e.printStackTrace(System.err);
                    System.exit(command.getStatus());
                }
            } else if ("--help".equals(args[0]) || "-h".equals(args[0])) {
                tool.generalHelp();
            } else {
                System.err.println(ElytronToolMessages.msg.commandOrAliasNotFound(args[0]));
                System.exit(ElytronToolExitStatus_unrecognizedCommand);
            }
        } else {
            // no arguments supplied, print general help message and exist.
            tool.generalHelp();
        }
    }

    private void generalHelp() {
        System.out.print(ElytronToolMessages.msg.missingArgumentsHelp());
        System.out.println();
        for (Command c: commandRegistry.values()) {
            c.help();
            System.out.println();
        }
    }

    Command findCommand(String commandName) {
        Command command = commandRegistry.get(commandName);
        if (command != null) {
            return command;
        } else {
            // check alias
            for (Command c: commandRegistry.values()) {
                if (c.isAlias(commandName)) {
                    return c;
                }
            }
        }
        return null;
    }

}
