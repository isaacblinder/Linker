/*Property of Isaac Blinder, 9/19/17 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Linker {
	//List of defined symbols, list of all modules
	public static ArrayList<Symbol> symbTable = new ArrayList<Symbol>();
	public static ArrayList<Module> modList = new ArrayList<Module>();

/*
	Pass one determines the base address for each module
	and produces a symbol table containing the absolute address for each defined symbol.
	It does this by building a list of every token, then using rules of the input to build each section of each module.
*/
	public static void firstPass(Scanner sc)
	{
		ArrayList<String> symbolNames = new ArrayList<String>();
		ArrayList<String> tokenList = new ArrayList<String>();
		
		//Add all the tokens to tokenList
		while(sc.hasNext()) {
			String[] splitLine = sc.next().split(" ");;
			
			//Add the tokens from the line
			for(String s: splitLine)
			{
				if(!("".equals(s)))
					tokenList.add(s);
			}
		}
	
int i = 1;
int loopNum = 0;
while(i < tokenList.size())
{
	int length = 0;
	Module mod = new Module();
	mod.numOfMod = loopNum;

	for(int d = 0; d < 3; d++)
	{
		int firstTok = Integer.parseInt((String) tokenList.get(i));
		i++;
		int l = 0;
		
		//definitions
		if(d == 0)
		{
			while(l < firstTok)	
			{
			Symbol sym = new Symbol((String) tokenList.get(i));
			i++;
			sym.relLoc = Integer.parseInt((String) tokenList.get(i));
			i++;
			
			//check that the symbol isn't already defined
			if(symbolNames.contains(sym.name))
			{
				System.out.println("ERROR: Symbol " + sym.name + " is defined multiple times. The first value is used.");
			}
			else
			{
			mod.defs.add(sym);
			symbolNames.add(sym.name);
			}
			l++;
			}
		}
		
		//uses
		else if(d == 1)
		{
			while(l < firstTok)
			{
				Symbol sym = new Symbol((String) tokenList.get(i));
				i++;
				sym.relLoc = Integer.parseInt((String) tokenList.get(i));
				i++;
				mod.uses.add(sym);
			l++;
			}
		}
		
		//text
		else if(d==2)
		{
			length = firstTok;
			for(int h = 0; h < firstTok; h++)
			{
				mod.text.add((String) tokenList.get(i));
				i++;
			}
		}
	}
	
	//set the size and base address for the module
	mod.size = length;
	if(loopNum == 0)
	{
		mod.base = 0;
	}
	else
	{
		mod.base = modList.get(loopNum-1).base + modList.get(loopNum-1).size;
	}
	modList.add(mod);
	
	
	

//build symbol table with base addresses of each symbol
for(Symbol def : mod.defs)
{
	//check that no relative location is bigger than the module
	if(def.relLoc >= mod.text.size())
	{
		System.out.println("ERROR: The definition of " + def.name + " is outside module " + mod.numOfMod + "; zero (relative) used.");
		def.relLoc = 0;
		def.absLoc = mod.base;
	}
	else
	{
	def.absLoc = def.relLoc + mod.base;
	}
	symbTable.add(def);
	

}

//this section looks at the relative address of uses to find the next location of the use.
for(int g = 0; g < mod.uses.size(); g++)
{
	Symbol u = mod.uses.get(g);
	String s = mod.text.get(u.relLoc);
	Integer lo = Integer.parseInt(s.substring(1, 4));
	
	//777 means the end of the uses of the symbol within the module
	while(lo != 777)
	{
		Symbol st = new Symbol();
		st.name = u.name;
		st.relLoc = lo;
		String s2 = mod.text.get(lo);
		lo = Integer.parseInt(s2.substring(1, 4));
		mod.uses.add(st);
	}
}

loopNum++;
}
}
	
	
	
/*
secondPass uses the base addresses and the symbol table computed in pass one to generate the actual output by 
relocating relative addresses and resolving external references.
*/
public static void secondPass(Scanner sc)
{
	for(Module mod : modList) {
	
		int c = 0;
		for(String text : mod.text)
		{
			
			
			Integer type = Integer.parseInt(text);
			
			//change relative addresses to absolute addresses
			if(type % 10 == 3)
			{
				type += (10 * mod.base);
				mod.text.set(c, type.toString());
			}
			
			//check that all external addresses are in the use list
			else if(type % 10 == 4)
			{
				int t = 0;
				for(Symbol s4 : mod.uses)
				{
					if(s4.relLoc == c)
						t = 1;
				}
				
				if(t == 0) {
					
					System.out.println("External address not in use list. Treated as immediate operand.");
					type -= 3;
					mod.text.set(c, Integer.toString(type));
				}
			}
			
			c++;
		}
		
		
		for(Symbol use : mod.uses)
		{
		
		String s = mod.text.get(use.relLoc);
		Integer i2 = Integer.parseInt(s);
	
		
		//check if there are any immediate addresses in the use list
		if(i2 % 10 == 1)
		{
			System.out.println("ERROR: Immediate address in a use list. Address will be treated as external");
		i2 += 3;
		mod.text.set(use.relLoc, i2.toString());
		}
		
		//resolve external addresses
			for(Symbol s2 : symbTable)
			{
				if(s2.name.equals(use.name))
				{
					String str = mod.text.get(use.relLoc).substring(0, 1);
					String numberAsString = String.format("%03d", s2.absLoc);
					String finalStr = str + numberAsString + "4";
					mod.text.set(use.relLoc, finalStr);
				}
			}
		
		//check to make sure each symbol used was also defined
		int trueDef = 0;
		for(int y = 0; y < symbTable.size(); y++)
		{
			if(use.name.equals(symbTable.get(y).name))
				trueDef = 1;
		}
		if(trueDef == 0) {
			System.out.println("ERROR: The symbol " + use.name + " is not defined; 0 used.");
			String oldStr = mod.text.get(use.relLoc);
			String newStr = oldStr.substring(0, 1) + "000" + oldStr.substring(4,5);
			mod.text.set(use.relLoc, newStr);
		}
		}
		
	}
}
	
	
	//final print method
	public static void printall()
	{
		
	//print symbol names and absolute locations
	System.out.println("Symbol Table:");
	for(Symbol def : symbTable)
	{
		System.out.println(def.name + " = " + def.absLoc);
	}
	
	//print each modules final memory output
	System.out.println("");
	System.out.println("");
	
	System.out.println("Memory Map:");
	int countLine = 0;
	for(Module m : modList) {
		System.out.println("MODULE " + m.numOfMod);
			
		
		for(String s : m.text) {
			//print the memory loc
			System.out.print(countLine + ": " + s.substring(0, 4));
			
			
			
			
			countLine++;
			System.out.println("");
		}
	System.out.println("");
	}
	
	//check that all of the defined symbols are used in the program
	for(Module m3 : modList) {
		for(Symbol s1 : m3.defs) {
			
			int used = 0;
			for(Module m2 : modList)
			{
				for(Symbol u2 : m2.uses)
				{
					if(u2.name.equals(s1.name)) 
						used = 1;
				}
			}	
		if(used == 0)
		{
			System.out.println("Warning: Symbol " +s1.name + " was defined in module " +  m3.numOfMod + " but was never used.");
		}
	}
	}
	}



	//main method
	public static void main(String[] args) {
		try {
		File f = new File(args[0]);
			Scanner input = new Scanner(f);
			
			firstPass(input);
			
			secondPass(input);
			
			printall();

		} 
		catch (FileNotFoundException e) {
			System.out.println("file not found.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
