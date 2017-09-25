
class Symbol {
public String name = "";

//relative and absolute location
public int relLoc;
public int absLoc;

//flags for conditions (for printing purposes)
//External 
int fl1 = 0;


//constructors
public Symbol()
{
}

public Symbol(String s, int l)
{
	name = s;
	relLoc = l;
}

public Symbol(String s)
{
	name = s;
}
}
