
package com.cognitionis.nlp_files.annotation_scorers;

import java.util.*;

/**
 *
 * @author Héctor Llorens
 * @since 2011
 */
public class Scomp {
    // Positive
    public HashMap<String, ArrayList<Judgement>> corr_increase;
    public HashMap<String, ArrayList<Judgement>> spur_decrease;
    // Negative
    public HashMap<String, ArrayList<Judgement>> corr_decrease;
    public HashMap<String, ArrayList<Judgement>> spur_increase;


    private HashMap<String, Double> p_imp;
    private HashMap<String, Double> r_imp;
    private HashMap<String, Double> f_imp;

    private HashMap<String, Double> p_imp_TokLevel;
    private HashMap<String, Double> r_imp_TokLevel;
    private HashMap<String, Double> f_imp_TokLevel;



    private HashMap<String, Double> p_erred;
    private HashMap<String, Double> r_erred;
    private HashMap<String, Double> f_erred;

    private HashMap<String, Double> p_erred_TokLevel;
    private HashMap<String, Double> r_erred_TokLevel;
    private HashMap<String, Double> f_erred_TokLevel;


    public Scomp(Score improved, Score base){

       // IMP: Scores must be sorted by numline (already checked when calculated...)

       corr_increase = new HashMap<String, ArrayList<Judgement>>();
       spur_decrease = new HashMap<String, ArrayList<Judgement>>();
       corr_decrease = new HashMap<String, ArrayList<Judgement>>();
       spur_increase = new HashMap<String, ArrayList<Judgement>>();

       p_imp=new HashMap<String, Double>();
       r_imp=new HashMap<String, Double>();
       f_imp=new HashMap<String, Double>();
       p_imp_TokLevel=new HashMap<String, Double>();
       r_imp_TokLevel=new HashMap<String, Double>();
       f_imp_TokLevel=new HashMap<String, Double>();
       p_erred=new HashMap<String, Double>();
       r_erred=new HashMap<String, Double>();
       f_erred=new HashMap<String, Double>();
       p_erred_TokLevel=new HashMap<String, Double>();
       r_erred_TokLevel=new HashMap<String, Double>();
       f_erred_TokLevel=new HashMap<String, Double>();


        for(String e:improved.getJudgements().keySet()){
            // Sacar la mejora de cada cosa de cada elemento
            p_imp.put(e,improvement(Score.oneDecPos(improved.getPrecision(e)),Score.oneDecPos(base.getPrecision(e))));
            r_imp.put(e,improvement(Score.oneDecPos(improved.getRecall(e)),Score.oneDecPos(base.getRecall(e))));
            f_imp.put(e,improvement(Score.oneDecPos(improved.getF1(e)),Score.oneDecPos(base.getF1(e))));

            p_imp_TokLevel.put(e,improvement(Score.twoDecPos(improved.getPrecisionTokenLevel(e)),Score.twoDecPos(base.getPrecisionTokenLevel(e))));
            r_imp_TokLevel.put(e,improvement(Score.twoDecPos(improved.getRecallTokenLevel(e)),Score.twoDecPos(base.getRecallTokenLevel(e))));
            f_imp_TokLevel.put(e,improvement(Score.twoDecPos(improved.getF1TokenLevel(e)),Score.twoDecPos(base.getF1TokenLevel(e))));
            
            p_erred.put(e,err_reduction(Score.oneDecPos(improved.getPrecision(e)),Score.oneDecPos(base.getPrecision(e))));
            r_erred.put(e,err_reduction(Score.oneDecPos(improved.getRecall(e)),Score.oneDecPos(base.getRecall(e))));
            f_erred.put(e,err_reduction(Score.oneDecPos(improved.getF1(e)),Score.oneDecPos(base.getF1(e))));

            p_erred_TokLevel.put(e,err_reduction(Score.twoDecPos(improved.getPrecisionTokenLevel(e))*100.0,Score.twoDecPos(base.getPrecisionTokenLevel(e))*100));
            r_erred_TokLevel.put(e,err_reduction(Score.twoDecPos(improved.getRecallTokenLevel(e))*100.0,Score.twoDecPos(base.getRecallTokenLevel(e))*100));
            f_erred_TokLevel.put(e,err_reduction(Score.twoDecPos(improved.getF1TokenLevel(e))*100,Score.twoDecPos(base.getF1TokenLevel(e))*100));
            

            // Guardar los judgements classificados en 4 tipos
            corr_increase.put(e, new ArrayList<Judgement>());
            spur_decrease.put(e, new ArrayList<Judgement>());
            corr_decrease.put(e, new ArrayList<Judgement>());
            spur_increase.put(e, new ArrayList<Judgement>());

            ArrayList<Judgement> improved_judgements=improved.getJudgements().get(e);
            ArrayList<Judgement> base_judgements=base.getJudgements().get(e);
            Judgement ji=null,jb=null;
            while(improved_judgements.size()>0 || base_judgements.size()>0){

                if(ji==null && improved_judgements.size()>0){
                    ji=improved_judgements.get(0);
                    improved_judgements.remove(0);
                }
                if(jb==null && base_judgements.size()>0){
                    jb=base_judgements.get(0);
                    base_judgements.remove(0);
                }


                // Same judgement?
                if(jb!=null && ji!=null && ji.getNumline()==jb.getNumline()){
                    if(ji.getJudgement_int()!=jb.getJudgement_int()){
                        if(ji.getJudgement_str().equals("corr")){
                           // System.out.println(":) Corr increase (miss/inco decrease)");
                           // System.out.println(ji.getKeylines()+"\n\n");
                            corr_increase.get(e).add(jb);
                        }
                        if(jb.getJudgement_str().equals("corr")){
                           // System.out.println(":( Corr decrease (miss/inco increase)");
                           // System.out.println(ji.getAnnotlines()+"\n\n");
                            corr_decrease.get(e).add(ji);
                        }
                    }
                    ji=null; jb=null;
                // treat and null the earlier
                }else{
                    if(jb==null || (ji!=null && ji.getNumline()<jb.getNumline())){
                        if(!ji.getJudgement_str().equals("spur")){
                            System.err.println("\n\nSpur EXPECTED (found "+ji.getJudgement_str()+") approach, line "+ji.getNumline());
                            System.err.println(ji.getNumline());
                            System.err.println(">>corr>>\n"+ji.getKeylines());
                            System.err.println("<<annot<<\n"+ji.getAnnotlines()+"\n");
                            if(jb!=null){
                                System.err.println("baseline: "+jb.getNumline());
                                System.err.println(">>corr>>\n"+jb.getKeylines());
                                System.err.println("<<annot<<\n"+jb.getAnnotlines()+"\n");
                            }
                            System.err.println("------ERROR------");
                            System.exit(0);
                        }
                           // System.out.println(":( Spur increased not desired");
                           // System.out.println(ji.getAnnotlines()+"\n\n");
                            spur_increase.get(e).add(ji);

                        ji=null;
                    }else{
                        if(!jb.getJudgement_str().equals("spur")){
                            System.err.println("\n\nSpur EXPECTED (found "+jb.getJudgement_str()+") approach, line "+jb.getNumline());
                            System.err.println(jb.getNumline());
                            System.err.println(">>corr>>\n"+jb.getKeylines());
                            System.err.println("<<annot<<\n"+jb.getAnnotlines()+"\n");
                            if(ji!=null){
                                System.err.println("baseline: "+ji.getNumline());
                                System.err.println(">>corr>>\n"+ji.getKeylines());
                                System.err.println("<<annot<<\n"+ji.getAnnotlines()+"\n");
                            }
                            System.err.println("------ERROR2------");
                            System.exit(0);
                        }
                           // System.out.println(":) Spur decrease");
                           // System.out.println(jb.getKeylines()+"\n\n");
                            spur_decrease.get(e).add(jb);
                        jb=null;
                    }
                }

            }




        }
    }


    public Double improvement(double improved, double base){
        return ((improved*100.0)/base)-100;
    }

    public Double err_reduction(double improved, double base){
        return ((((100-base)-(100-improved))/(100-base))*100.0);
    }

    public void print(){
        System.out.println("Print comparative\n\n");


        for(String e:p_imp.keySet()){
            // Por cada elemento
            System.out.println(">"+e.toUpperCase());
            // Imprimir mejora (de cada cosa)
            System.out.println("\t REGULAR IMPROVEMENT \tPrecision=" + Score.oneDecPos(p_imp.get(e)) + " (err_reduct "+Score.oneDecPos(p_erred.get(e))+") %\tRecall=" + Score.oneDecPos(r_imp.get(e)) + " (err_reduct "+Score.oneDecPos(r_erred.get(e))+") %\tF1=" + Score.oneDecPos(f_imp.get(e)) + " (err_reduct "+Score.oneDecPos(f_erred.get(e))+") %\t- corr_inc="+corr_increase.get(e).size()+"; spur_dec="+spur_decrease.get(e).size()+" -- corr_dec="+corr_decrease.get(e).size()+"; spur_inc="+spur_increase.get(e).size());
            System.out.println("\t Token level impr\tPrecision=" + Score.oneDecPos(p_imp_TokLevel.get(e)) + " (err_reduct "+Score.oneDecPos(p_erred_TokLevel.get(e))+") %\tRecall=" + Score.oneDecPos(r_imp_TokLevel.get(e)) + " (err_reduct "+Score.oneDecPos(r_erred_TokLevel.get(e))+") %\tF1=" + Score.oneDecPos(f_imp_TokLevel.get(e))+" (err_reduct "+Score.oneDecPos(f_erred_TokLevel.get(e))+") %");

            // Imprimir los casos ordenados...
        System.out.println("\nCorr increase cases\n");
        for(Judgement j:corr_increase.get(e)){
            System.out.println(">>corr>>\n"+j.getKeylines());
            System.out.println("<<annot<<\n"+j.getAnnotlines()+"\n");
        }

        System.out.println("\nSpur decrease cases\n");
        for(Judgement j:spur_decrease.get(e)){
            System.out.println(j.getAnnotlines()+"\n");
        }


        System.out.println("\nCorr decrease cases\n");
        for(Judgement j:corr_decrease.get(e)){
            System.out.println(">>corr>>\n"+j.getKeylines());
            System.out.println("<<annot<<\n"+j.getAnnotlines()+"\n");
        }


        System.out.println("\nSpur increase cases\n");
        for(Judgement j:spur_increase.get(e)){
            System.out.println(j.getAnnotlines()+"\n");
        }


        }
    }

}
