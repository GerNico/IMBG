package spr;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.util.ArrayList;
import java.util.Collection;

 class SmartExpFit {

    boolean    incrExp;
    double []  y0_A_k;
    SmartExpFit(double[] x,double[] y){
        if(x.length==y.length){
            // Collect data.
            final WeightedObservedPoints obs = new WeightedObservedPoints();
            for(int i=0;i<x.length;i++)obs.add(x[i],y[i]);
            final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(6);
            final double[] coefPoly = fitter.fit(obs.toList());
            PolynomialFunction polyFunc=new PolynomialFunction(coefPoly);
            PolynomialFunction funcDeriv=polyFunc.polynomialDerivative();
            // make main fit data
            incrExp = y[0] < y[y.length - 1];
            if (incrExp){
                //y=y0+A*(1-Exp(-k(t-t0)))      ln (dy/dt)=ln(kA)-k(t-t0)
                SimpleRegression regression = new SimpleRegression();
                for (int i=0;i<y.length;i++){
                    double logD=Math.log(funcDeriv.value(x[i]));
                    if(!Double.isNaN(logD)) regression.addData(x[i]-x[0],logD);}
                double lnkA=regression.getIntercept();
                double k_=-regression.getSlope();

                double[] w=new double[x.length];double[] xi=new double[x.length];double[] yi=new double[x.length];
                for(int i=0;i<x.length;i++)
                {   w[i]=1.0;     xi[i]=x[i]-x[0];      yi[i]=y[i];}

                GrowingExp grow =new GrowingExp();
                this.y0_A_k=grow.getFit(xi,yi,w,new double[]{y[0],Math.exp(lnkA)/k_,k_});
            }
            else{
                //y=y0-A*Exp(-k(t-t0))          ln (-dy/dt)=ln(kA)-k(t-t0)
                SimpleRegression regression = new SimpleRegression();
                for (int i=0;i<y.length;i++)
                    {double logD=Math.log(-funcDeriv.value(x[i]));
                    if(!Double.isNaN(logD))regression.addData(x[i]-x[0],logD);}
                double lnkA=regression.getIntercept();
                double k_=-regression.getSlope();

                double[] w=new double[x.length];double[] xi=new double[x.length];double[] yi=new double[x.length];
                for(int i=0;i<x.length;i++)
                {   w[i]=1.0;     xi[i]=x[i]-x[0];      yi[i]=y[i];}

                DecrExp decr =new DecrExp();
                this.y0_A_k=decr.getFit(xi,yi,w,new double[]{y[0],Math.exp(lnkA)/k_,k_});
            }
        }else System.out.println("Bad data");

    }

    //----------------------------          AbstractCurveFitter Decreasing Exponent
    private class DecrExp implements ParametricUnivariateFunction {

        public double value(double t, double... parameters) {
            return parameters[0] + parameters[1] *  Math.exp(-parameters[2] * t);
        }

        // Jacobian matrix of the above. In this case, this is just an array of
        // partial derivatives of the above function, with one element for each parameter.
        public double[] gradient(double t, double... parameters) {
            final double A = parameters[1];
            final double k = parameters[2];

            return new double[]{
                    1,
                    Math.exp(-k * t),
                    -A * Math.exp(-k * t) * t
            };
        }


        private class MyFuncFitter extends AbstractCurveFitter {
            double[] initialGuess;
            MyFuncFitter(double[] iniGues)
            {   super();
                this.initialGuess=iniGues;}
            protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
                final int len = points.size();
                final double[] target  = new double[len];
                final double[] weights = new double[len];

                int i = 0;
                for(WeightedObservedPoint point : points) {
                    target[i]  = point.getY();
                    weights[i] = point.getWeight();
                    i += 1;
                }

                final AbstractCurveFitter.TheoreticalValuesFunction model = new
                        AbstractCurveFitter.TheoreticalValuesFunction(new DecrExp(), points);

                return new LeastSquaresBuilder().
                        maxEvaluations(Integer.MAX_VALUE).
                        maxIterations(Integer.MAX_VALUE).
                        start(initialGuess).
                        target(target).
                        weight(new DiagonalMatrix(weights)).
                        model(model.getModelFunction(), model.getModelFunctionJacobian()).
                        build();
            }}

        double[] getFit(double[] x, double[] y, double[] w, double[] iniGes) {
            DecrExp.MyFuncFitter fitter = new DecrExp.MyFuncFitter(iniGes);
            ArrayList<WeightedObservedPoint> points = new ArrayList<>();

            // Add points here; for instance,
            if((x.length==y.length)&&(y.length==w.length))for(int i=0;i<x.length;i++){
                WeightedObservedPoint point = new WeightedObservedPoint(w[i],x[i],y[i]);
                points.add(point);}

            return fitter.fit(points);
        }
    }
    //----------------------------          AbstractCurveFitter Increasing Exponent
    private class GrowingExp implements ParametricUnivariateFunction {

        public double value(double t, double... parameters) {
            return parameters[0] + parameters[1] * (1 - Math.exp(-parameters[2] * t));
        }

        // Jacobian matrix of the above. In this case, this is just an array of
        // partial derivatives of the above function, with one element for each parameter.
        public double[] gradient(double t, double... parameters) {
            final double A = parameters[1];
            final double k = parameters[2];

            return new double[]{
                    1,
                    1 - Math.exp(-k * t),
                    A * Math.exp(-k * t) * t
            };
        }


        private class MyFuncFitter extends AbstractCurveFitter {
            double[] initialGuess;
            MyFuncFitter(double[] iniGues)
            {   super();
                this.initialGuess=iniGues;}
            protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
                final int len = points.size();
                final double[] target  = new double[len];
                final double[] weights = new double[len];

                int i = 0;
                for(WeightedObservedPoint point : points) {
                    target[i]  = point.getY();
                    weights[i] = point.getWeight();
                    i += 1;
                }

                final AbstractCurveFitter.TheoreticalValuesFunction model = new
                        AbstractCurveFitter.TheoreticalValuesFunction(new GrowingExp(), points);

                return new LeastSquaresBuilder().
                        maxEvaluations(Integer.MAX_VALUE).
                        maxIterations(Integer.MAX_VALUE).
                        start(initialGuess).
                        target(target).
                        weight(new DiagonalMatrix(weights)).
                        model(model.getModelFunction(), model.getModelFunctionJacobian()).
                        build();
            }}

        double[] getFit(double[] x, double[] y, double[] w, double[] iniGes) {
            MyFuncFitter fitter = new MyFuncFitter(iniGes);
            ArrayList<WeightedObservedPoint> points = new ArrayList<>();

            // Add points here; for instance,
            if((x.length==y.length)&&(y.length==w.length))for(int i=0;i<x.length;i++){
                WeightedObservedPoint point = new WeightedObservedPoint(w[i],x[i],y[i]);
                points.add(point);}

            return fitter.fit(points);
        }
    }
}
