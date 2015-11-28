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
        PING_FLAG("-p"),
        TIMEOUT_FLAG("-t"),
        LIST_FLAG("-l"),
        HELP_FLAG("-h"),
        MULTITHREADING_FLAG("-m"),
        ERROR_FLAG("-e"),
        EXIT_FLAG("-x");
        private final String flag;
        Flags(String flag){
            this.flag = flag;
        }
        public static Flags getEnum(String flag){
            switch(flag){
                case "-p": return PING_FLAG;
                case "-l": return LIST_FLAG;
                case "-h": return HELP_FLAG;
                case "-m": return MULTITHREADING_FLAG;
                case "-t": return TIMEOUT_FLAG;
                case "-x": return EXIT_FLAG;
                default:
                    return ERROR_FLAG;
            }
        }
        public boolean isEqual(String flag){
            return this.flag.equals(flag);
        }
}
