package com.wei.ChemCalc;

/*

Last Modified by Wei Shi 12/07/2015

*/

public class Element {
    private Integer _atomicNumber;
    private String _symbol;
    private String _name;
    private Double _molarMass;


    public void setAtomicNumber(int id) {
        this._atomicNumber = id;
    }
    public void setName(String name) {
        this._name = name;
    }
    public void setSymbol(String symbol) {
        this._symbol = symbol;
    }

    public void setMolarMass(Double molarMass) {
        this._molarMass = molarMass;
    }


    public int getAtomicNumber() {
        return _atomicNumber;
    }
    public String getName() {
        return _name;
    }
    public String getSymbol() {
        return _symbol;
    }

    public Double getMolarMass() {
        return _molarMass;
    }

    @Override
    public String toString() {
        return " Atomic Number = " +_atomicNumber
                + "\n Symbol = " + _symbol
                + "\n Name = " + _name
                + "\n Molar Mass= " + _molarMass;

    }
}