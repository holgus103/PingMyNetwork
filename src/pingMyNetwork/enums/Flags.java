/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.enums;

     /**
     * @author Jakub Suchan
     * @version %I%, %G%
     * @since 1.0
     * Enum storing different flags
     */
public enum Flags {

    /**
     * Flag for error pinging
     */
    PING_FLAG("-p"),

    /**
     * Flag for timeout
     */
    TIMEOUT_FLAG("-t"),

    /**
     * Flag for list
     */
    LIST_FLAG("-l"),

    /**
     * Flag for help
     */
    HELP_FLAG("-h"),

    /**
     * Flag for error
     */
    ERROR_FLAG("-e"),

    /**
     * Flag for exit
     */
    EXIT_FLAG("-x");
        private final String flag;
        Flags(String flag){
            this.flag = flag;
        }

    /**
     * Returns an enum for the supplied flag
     * @param flag Flag to generate the enum
     * @return Enum for the supplied flag
     */
    public static Flags getEnum(String flag){
            switch(flag){
                case "-p": return PING_FLAG;
                case "-l": return LIST_FLAG;
                case "-h": return HELP_FLAG;
                case "-t": return TIMEOUT_FLAG;
                case "-x": return EXIT_FLAG;
                default:
                    return ERROR_FLAG;
            }
        }

    /**
     * Compares flag with an enum
     * @param flag Flag to be compared
     * @return Returns whether the flag is equal
     */
    public boolean isEqual(String flag){
            return this.flag.equals(flag);
        }
}
