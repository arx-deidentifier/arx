#check operating system and remove SWT libraries for other systems.
SCRIPT_DIR=$(readlink -f ${0%/*})
echo $SCRIPT_DIR
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "      LINUX SYSTEM"
     	rm $SCRIPT_DIR\lib\ant\eclipse\org.eclipse.swt.cocoa.macosx.x86_64_3.114.100.v20200604-0951.jar
		rm $SCRIPT_DIR\lib\ant\eclipse\org.eclipse.swt.win32.win32.x86_64_3.114.100.v20200604-0951.jar

elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo "      Mac OSX SYSTEM"
		rm $SCRIPT_DIR\lib\ant\eclipse\org.eclipse.swt.gtk.linux.x86_64_3.114.100.v20200604-0951.jar
		rm $SCRIPT_DIR\lib\ant\eclipse\org.eclipse.swt.win32.win32.x86_64_3.114.100.v20200604-0951.jar
elif [[ "$OSTYPE" == "win32" ]]; then
        echo "      MS Windows SYSTEM"
		rm $SCRIPT_DIR/lib/ant/eclipse/org.eclipse.swt.cocoa.macosx.x86_64_3.114.100.v20200604-0951.jar
		rm $SCRIPT_DIR/lib/ant/eclipse/org.eclipse.swt.gtk.linux.x86_64_3.114.100.v20200604-0951
elif [[ "$OSTYPE" == "cygwin" ]]; then
        echo "      MS Windows cygwin SYSTEM"
		rm $SCRIPT_DIR/lib/ant/eclipse/org.eclipse.swt.cocoa.macosx.x86_64_3.114.100.v20200604-0951.jar
		rm $SCRIPT_DIR/lib/ant/eclipse/org.eclipse.swt.gtk.linux.x86_64_3.114.100.v20200604-0951
elif [[ "$OSTYPE" == "msys" ]]; then
        echo "      MS Windows GitBash SYSTEM"
		rm $SCRIPT_DIR/lib/ant/eclipse/org.eclipse.swt.cocoa.macosx.x86_64_3.114.100.v20200604-0951.jar
		rm $SCRIPT_DIR/lib/ant/eclipse/org.eclipse.swt.gtk.linux.x86_64_3.114.100.v20200604-0951.jar
else
        # Unknown.
		echo "==============================="
		echo "      Unknown SYSTEM, please remove extra swt*.jar libraries manually!!!"
		echo "==============================="
fi


