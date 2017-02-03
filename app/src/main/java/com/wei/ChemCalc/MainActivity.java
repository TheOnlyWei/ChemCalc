package com.wei.ChemCalc;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;


/*

Polyatomic database
parenthesis recognizer
Recognize molecules such as: CH3(CH2)6CO2H


*/
/*

public static void printMap(Map mp) {
    Iterator it = mp.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        System.out.println(pair.getKey() + " = " + pair.getValue());
        it.remove(); // avoids a ConcurrentModificationException
    }
}
*/
/*

Last Modified by Wei Shi 12/07/2015

*/

public class MainActivity extends Activity {
    String SI_unit = "normal";
    EditText fromChem;
    EditText toChem;
    TextView topTotalMass;
    TextView botTotalMass;
    Button clearAll;
    Button calculate;
    Map<String, Map<String, Integer>> fromMap;
    Map<String, Map<String, Integer>> toMap;
    Vector<String> fromVector;
    Vector<String> toVector;
    Spinner spinner;


    private class validElement{
        boolean valid = true;
        validElement(boolean tf){
            valid = tf;

        }
    }

    private class Unit{
        String Prefix ="";
        Double Mass;

        Unit(Double ma, String pre){
            Prefix = Prefix;
            Mass=ma;
        }

    }
    private class Index{
        int index;

        Index( int i){
            index =i;
        }
        void inc() {
            index++;
        }
        void dec(){
            index--;
        }
        void reset(){
            index =0;

        }
        public String toString(){
            return ""+index;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageView image = new ImageView(getApplicationContext());

        fromChem = (EditText) findViewById(R.id.chemEqFrom);
        toChem = (EditText) findViewById(R.id.chemEqTo);
        topTotalMass = (TextView) findViewById(R.id.topTotalMassCalc);
        botTotalMass = (TextView) findViewById(R.id.botTotalMassCalc);


        image.setImageResource(R.drawable.ic_arrow_downward_black_48dp);

        calculate = (Button) findViewById(R.id.balance);

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String from = fromChem.getText().toString();
                fromMap = getMap(from);
                fromVector = getVectorNoCoef(from);
                //System.out.println("molecules after getMap: " + fromMap);
                //System.out.println("fromVector: " + fromVector);
                if (fromMap != null && !from.equals("")) {
                    //System.out.println("fromMap: " + fromMap);
                    //Toast.makeText(MainActivity.this, "" + fromMap, Toast.LENGTH_LONG).show();
                    Vector<MyEntry<String, Double>> massList = getMolarMass(fromMap, fromVector);

                    //Toast.makeText(MainActivity.this, "" + massList, Toast.LENGTH_LONG).show();
                    Unit unit = new Unit(0.0, "");
                    for (int i = 0; i < massList.size(); i++) {
                        unit.Mass += massList.elementAt(i).getValue();
                    }

                    DecimalFormat df = new DecimalFormat("#.######");
                    df.setRoundingMode(RoundingMode.CEILING);
                    unitConversion(unit);
                    unit.Mass = Double.parseDouble(df.format( unit.Mass));

                    topTotalMass.setText(" " +  unit.Mass.toString() + " " + Html.fromHtml(unit.Prefix) +"g");

                }

                String to = toChem.getText().toString();
                toMap = getMap(to);
                toVector = getVectorNoCoef(to);

                if (toMap != null && !to.equals("")) {
                    //System.out.println("toMap: " + toMap);
                    //Toast.makeText(MainActivity.this, "" + toMap, Toast.LENGTH_LONG).show();
                    Vector<MyEntry<String, Double>> massList = getMolarMass(toMap, toVector);

                    //Toast.makeText(MainActivity.this, "" + massList, Toast.LENGTH_LONG).show();
                    Unit unit = new Unit(0.0, "");
                    for (int i = 0; i < massList.size(); i++) {
                        unit.Mass += massList.elementAt(i).getValue();
                    }
                    DecimalFormat df = new DecimalFormat("#.######");
                    df.setRoundingMode(RoundingMode.CEILING);
                    unitConversion(unit);
                    unit.Mass= Double.parseDouble(df.format( unit.Mass));
                    botTotalMass.setText(" " +  unit.Mass.toString() + " " + Html.fromHtml(unit.Prefix) +"g");

                }

                else if(from.equals("") && to.equals("")){
                    Toast.makeText(MainActivity.this, "You haven't entered anything.", Toast.LENGTH_SHORT).show();

                }

            }
        });

        clearAll = (Button) findViewById(R.id.clear);
        clearAll.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                fromChem.setText("");
                toChem.setText("");

            }
        });

        spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.mass_units, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(4);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SI_unit = spinner.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

    }

    public void unitConversion(Unit unit){
        switch (SI_unit){
            case "mega":{
                unit.Mass = unit.Mass * Math.pow(10.0, -6.0);
                unit.Prefix = "M";
                break;
            }
            case "kilo":{
                unit.Mass = unit.Mass* Math.pow(10.0, -3.0);
                unit.Prefix  = "k";
                break;
            }
            case "hecto":{
                unit.Mass = unit.Mass * Math.pow(10.0, -2.0);
                unit.Prefix  = "h";
                break;
            }
            case "deka":{
                unit.Mass = unit.Mass* Math.pow(10.0, -1.0);
                unit.Prefix  = "da";
                break;
            }
            case "normal":{
                //do nothing, already normal
                break;
            }
            case "deci":{
                unit.Mass = unit.Mass* Math.pow(10.0, 1.0);
                unit.Prefix  = "d";
                break;
            }
            case "centi":{
                unit.Mass = unit.Mass * Math.pow(10.0, 2.0);
                unit.Prefix  = "c";
                break;
            }
            case "milli":{
                unit.Mass = unit.Mass * Math.pow(10.0, 3.0);
                unit.Prefix  = "m";
                break;
            }
            case "micro":{
                unit.Mass = unit.Mass * Math.pow(10.0, 6.0);
                unit.Prefix  = "&#956;";
                break;
            }

        }

    }
 /*
    //requires vectors of molecular strings without coefficient.
    public void balance(Vector<String> from, Vector<String> to, Map<String, Map<String, Integer>> fMap, Map<String, Map<String, Integer>> tMap){


    }
*/
    public static void printMap(Map mp) {
        //System.out.println("@@@Testing printer...");
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    public Vector<MyEntry<String, Double>> getMolarMass(Map<String, Map<String, Integer>> moleMap, Vector<String> moleVec){

        Double mass = 0.0;
        Map<String, Integer> temp;
        String URL = "content://com.wei.ChemCalc.PeriodicTable/elements";
        Uri element = Uri.parse(URL);
        Cursor c;
        Vector<MyEntry<String, Double>> moleMass = new Vector<>();
        String curMolecule = "";
        Integer curMol;
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);

        for(int i=0; i<moleVec.size(); i++){
            temp = moleMap.get(moleVec.elementAt(i));
            curMolecule = moleVec.elementAt(i);
            //System.out.println("Current molecule: " + curMolecule);
            //System.out.println("Current molecule's info: " + temp);
            Iterator it = temp.entrySet().iterator();
            Map.Entry pair;
            //iterate through each element to find their moles and molemass
            while(it.hasNext()){

                pair = (Map.Entry) it.next();
                //System.out.println(pair.getKey() + " = " + pair.getValue());

                c = getContentResolver().query(element, new String[]{PeriodicTable.MOLAR_MASS}, PeriodicTable.SYMBOL + " = '" + pair.getKey() +"'",  null, "molarMass");
                c.moveToFirst();

                if(c.getCount() > 0) {
                    Double massTemp = Double.parseDouble(c.getString(c.getColumnIndex(PeriodicTable.MOLAR_MASS)));
                    Integer mol = (Integer) pair.getValue();
                    massTemp = massTemp  * mol.doubleValue();

                    //System.out.println("" + pair.getKey() + " moles: " + c.getString(c.getColumnIndex(PeriodicTable.MOLAR_MASS)));
                    //System.out.println("mass: " + massTemp);
                    //System.out.println("mol: " + mol);
                    mass = mass + massTemp;

                }

                c.close();
            }

            Integer mol = moleMap.get(curMolecule).get("mol");
            mass = mass * mol.doubleValue();
            mass = Double.parseDouble(df.format(mass));
            //System.out.println("Current molecular mass: " + mass);
            MyEntry<String, Double> entry = new MyEntry<>(curMolecule, mass);
            moleMass.add(entry);
            mass = 0.0;


        }

        return moleMass;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //parses and stores digits from a string from curIndex until it encounters a non-digit.
    //123C -> curIndex = 0 with value '1' and ends at curIndex = 3 with value 'C'.
    public Integer parseCoefficient(String formula, Index curIndex){
        Integer count = 0;

        for(; curIndex.index < formula.length() && Character.isDigit(formula.charAt(curIndex.index)); curIndex.inc()){
            count *= 10;
            count += Character.getNumericValue(formula.charAt(curIndex.index));
        }
        //System.out.println("mol coefficient: " + count);
        return count;
    }

    public Vector<String> getVectorNoCoef(String formula){
        Vector<String> ret = new Vector<String>();
        String currentMolecule = "";
        boolean parsingCoef = false;
        boolean outOfBounds = false;
        if(formula.equals("")){
            return ret;
        }

        if(Character.isDigit(formula.charAt(0)) )
            parsingCoef = true;

        //skip mol coefficient
        for(int i =0; i<formula.length(); i++){
            for(; i< formula.length() && formula.charAt(i) == ' '; i++){
            }
            //skip mol coefficient
            while(parsingCoef){
                //System.out.println("skipping : " + formula.charAt(i));
                i++;
                if(i >= formula.length()) {
                    outOfBounds = true;
                    break;
                }

                else if(!Character.isDigit(formula.charAt(i))){
                    parsingCoef = false;
                    i--;
                    //System.out.println("" + formula.charAt(i) + " is not a digit, back up an iteration and end parsingCoef.");
                }
            }

            if(outOfBounds){
                break;
            }
            else if(Character.isDigit(formula.charAt(i)) ||  Character.isLetter(formula.charAt(i))){
                currentMolecule = currentMolecule + formula.charAt(i);
            }

            else if(formula.charAt(i) == '+'&& !currentMolecule.equals("") ){
                parsingCoef = true;
                ret.add(currentMolecule);
                currentMolecule = "";

            }

            if(i+1 == formula.length() && !currentMolecule.equals("")){
                ret.add(currentMolecule);
                currentMolecule = "";
            }
            //System.out.println("i : " + i);
            //System.out.println("current molecule: " + currentMolecule);
        }

        return ret;
    }

    public Vector<String> getVectorWithCoef(String formula){
        Vector<String> ret = new Vector<String>();
        String currentMolecule = "";
        //skip mol coefficient
        for(int i =0; i<formula.length(); i++){
            for(; i< formula.length() && formula.charAt(i) == ' '; i++){
            }

            if(Character.isDigit(formula.charAt(i)) ||  Character.isLetter(formula.charAt(i))){
                currentMolecule = currentMolecule + formula.charAt(i);
            }

            else if(formula.charAt(i) == '+'&& !currentMolecule.equals("") ){
                ret.add(currentMolecule);
                currentMolecule = "";

            }
            if(i+1 == formula.length() && !currentMolecule.equals("")){
                ret.add(currentMolecule);
                currentMolecule = "";
            }

        }

        return ret;
    }
    //parses and changes index to before non-digit.
    public Integer parseAmount(String curMolecule, Index curIndex){
        Integer count =0;
        //keep looping as long as we know the current index is a digit
        for(; curIndex.index < curMolecule.length() && Character.isDigit(curMolecule.charAt(curIndex.index)); curIndex.inc()){
            count *= 10;
            count += Character.getNumericValue(curMolecule.charAt(curIndex.index));
        }

        curIndex.dec();
        return count;
    }
    //At most changes to the index before next letter. Molecule map is the map for a specific molecule.
    public void putInMoleculeMap(String currentElement, String curMolecule,  Map<String, Integer> elementCount, Index curIndex){

        //currentElement is already in map and next character is a digit denoting amount
        if(elementCount.containsKey(currentElement) && curIndex.index+1 < curMolecule.length() && Character.isDigit(curMolecule.charAt(curIndex.index + 1)) ){
            curIndex.inc();//increment index to letter that is a digit
            Integer newAmount = parseAmount(curMolecule, curIndex);
            Integer currentAmount = elementCount.get(currentElement);
            elementCount.put(currentElement, currentAmount + newAmount);

        }
        else if(elementCount.containsKey(currentElement) && curIndex.index+1 < curMolecule.length() && !Character.isDigit(curMolecule.charAt(curIndex.index + 1)) ) {
            Integer currentAmount = elementCount.get(currentElement);
            elementCount.put(currentElement, currentAmount + 1);

        }
        else if(!elementCount.containsKey(currentElement) && curIndex.index+1 < curMolecule.length() &&  Character.isDigit(curMolecule.charAt(curIndex.index + 1)) ){
            curIndex.inc();//increment index to letter that is a digit
            Integer amount = parseAmount(curMolecule, curIndex);
            elementCount.put(currentElement, amount);
        }
        else if(!elementCount.containsKey(currentElement) && curIndex.index+1 < curMolecule.length() && !Character.isDigit(curMolecule.charAt(curIndex.index + 1)) ){
            elementCount.put(currentElement, 1);
        }

        //End of string exceptions
        else if(elementCount.containsKey(currentElement) && curIndex.index == curMolecule.length()-1){
            curIndex.inc();//increment index to letter that is a digit
            Integer currentAmount = elementCount.get(currentElement);
            elementCount.put(currentElement, currentAmount + 1);

        }
        else if(!elementCount.containsKey(currentElement) && curIndex.index == curMolecule.length()-1){
            elementCount.put(currentElement, 1);

        }
    }

    //simply puts current elementCount into the map of molecules or the map for the whole formula. Formula map is the map for the whole string.
    public void putInFormulaMap(String curMolecule, Map<String, Integer> elementCount,  Map<String, Map<String, Integer>> molecules){
        if(molecules.containsKey(curMolecule)){
            Integer curMol = molecules.get(curMolecule).get("mol");
            Integer newMol = elementCount.get("mol");

            Integer totalMol = curMol + newMol;
            elementCount.put("mol", totalMol);

            //need to allocate dynamically otherwise data is not stored
            Map<String, Integer> temp = new TreeMap<String, Integer>(elementCount);
            molecules.put(curMolecule, temp);

        }
        else if(!molecules.containsKey(curMolecule)){
            //need to allocate dynamically otherwise data is not stored
            Map<String, Integer> temp = new TreeMap<String, Integer>(elementCount);
            molecules.put(curMolecule, temp);
        }

    }

    public String getStructure(String curMolecule){
        String structure = "";

        if(!Character.isLetter(curMolecule.charAt(0))) {
            int i=0;
            //do nothing counting loops for the purpose of incrementing i if certain characters are detected.
            for (; i < curMolecule.length() && curMolecule.charAt(i) == ' ' && curMolecule.charAt(i) == '+'; i++) {
            }

            for (; i < curMolecule.length() && Character.isDigit(curMolecule.charAt(i)); i++) {
            }

            for(; i<curMolecule.length(); i++){
                structure = structure+curMolecule.charAt(i);
            }

        }
        else structure = curMolecule;

        return structure;
    }

    public Map<String, Map<String, Integer>> getMap(String formula) {
        if(formula.equals("")){
            return null;
        }


        String URL = "content://com.wei.ChemCalc.PeriodicTable/elements";
        Uri element = Uri.parse(URL);

        Vector<String> moleculeVector;
        Map<String, Integer> elementCount = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Map<String, Integer>> molecules = new TreeMap<String, Map<String, Integer>>(String.CASE_INSENSITIVE_ORDER);

        //boolean newMolecule = true;
        boolean parsingCoef = true;
        validElement validElement = new validElement(true);
        String currentElement = "";
        Integer currentCoef = 0;

        Cursor c;
        Index curIndex = new Index(0);


        moleculeVector = getVectorWithCoef(formula);

        //System.out.println("vector with coef : " + moleculeVector);
        //iterating through vector of molecules
        for(int i =0; i< moleculeVector.size(); i++){
            String curMolecule = moleculeVector.get(i);
            //System.out.println("@@@@@NEW MOLECULE@@@@@@");
            //System.out.println("curMolecule: " + curMolecule);
            String moleculeStruct = getStructure(curMolecule);
            //System.out.println("moleculeStruct: " + moleculeStruct);

            //parsing the molecule
            for(; curIndex.index<curMolecule.length() && validElement.valid; curIndex.inc()){
                //System.out.println("curIndex.index: " + curIndex.index);
                Character ch = curMolecule.charAt(curIndex.index);

                //coefficient parsing
                if (Character.isDigit(ch) && parsingCoef) {
                    //System.out.println("ch is a digit: " + ch);
                    currentCoef = parseCoefficient(curMolecule, curIndex);
                    //System.out.println("currentCoef: " + currentCoef);
                    ch = curMolecule.charAt(curIndex.index);
                    //System.out.println("ch after parseCoefficient: " + ch);
                }
                //System.out.println("curIndex.index: " + curIndex.index);
                //System.out.println("ch: " + ch);
                if (!Character.isDigit(ch)&& parsingCoef) {
                    //System.out.println("ch is a letter, currentCoef: " + currentCoef);
                    if (currentCoef != 0)
                        elementCount.put("mol", currentCoef);
                    else
                        elementCount.put("mol", 1);

                    parsingCoef = false;
                    currentCoef = 0;

                }
                //first character is always a non-digit since we just parsed all digits initially.

                String guess = "";

                Character next;

                //try to guess current element as a combination of current letter and next letter(at most length 2)
                if (curIndex.index+1 < curMolecule.length()) {
                    next = curMolecule.charAt(curIndex.index + 1);
                    guess = "" + ch + next;
                    //System.out.println("guess: " + guess);
                    c = getContentResolver().query(element, new String[]{PeriodicTable.SYMBOL}, PeriodicTable.SYMBOL + " =  '" + guess +"'",  null, "symbol");

                    //guessed element not in PT
                    if (c.getCount() <= 0) {

                        currentElement = "" + ch;

                        //guess is false, then check to see if current character is an element in the PT.
                        c = getContentResolver().query(element, new String[]{PeriodicTable.SYMBOL}, PeriodicTable.SYMBOL + " = '" + ch +"'",  null, "symbol");

                        //current single letter element is also invalid
                        if (c.getCount() <= 0) {
                            //System.out.println("invalid element");
                            validElement.valid = false;
                        }
                        //current single letter element is in the PT
                        else{
                            //System.out.println("useCh: " + ch);
                            putInMoleculeMap(currentElement, curMolecule, elementCount, curIndex);

                        }

                    }
                    //guessed element is in PT
                    else {
                        //System.out.println("useGuess");
                        currentElement = guess;
                        curIndex.inc(); //if guess is correct, we increment the index
                        putInMoleculeMap(currentElement, curMolecule, elementCount, curIndex);

                    }
                    c.close();
                }
                //Since we are at the last character, we can't possibly guess.
                else if(curIndex.index+1 == curMolecule.length()){
                    //System.out.println("" + ch + " is last char of " + curMolecule);
                    c = getContentResolver().query(element, new String[]{PeriodicTable.SYMBOL}, PeriodicTable.SYMBOL + " = '" + ch +"'", null, "symbol");

                    if (c.getCount() <= 0) {
                        //System.out.println("last ch, invalid element");
                        validElement.valid= false;

                    }
                    else {
                        //System.out.println("last ch, use: " + ch);
                        currentElement = ""+ch;
                        putInMoleculeMap(currentElement, curMolecule, elementCount, curIndex);
                    }
                    c.close();
                }

            }
            //END OF LOOP FOR RETRIEVING MOLECULE
            //System.out.println("elementCount " + elementCount);
            //System.out.println("End of iteration: " + curIndex.index);
            //System.out.println("molecules: " + molecules);
            putInFormulaMap(moleculeStruct, elementCount, molecules);
            elementCount.clear();
            parsingCoef = true;
            curIndex.reset();
        }

        if (validElement.valid)
            return molecules;

        else {
            Toast.makeText(this, "Invalid formula, please correct.", Toast.LENGTH_SHORT).show();
            return null;
        }



    }


}
