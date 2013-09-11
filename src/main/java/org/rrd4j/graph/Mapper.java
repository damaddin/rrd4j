package org.rrd4j.graph;

import java.util.Iterator;

import org.rrd4j.backend.spi.RobinIterator;
import org.rrd4j.backend.spi.RobinTimeSet;

class Mapper {
    private RrdGraphDef gdef;
    private ImageParameters im;
    private double pixieX, pixieY;

    Mapper(RrdGraph rrdGraph) {
        this.gdef = rrdGraph.gdef;
        this.im = rrdGraph.im;
        pixieX = (double) im.xsize / (double) (im.end - im.start);
        if (!gdef.logarithmic) {
            pixieY = (double) im.ysize / (im.maxval - im.minval);
        }
        else {
            pixieY = (double) im.ysize / (ValueAxisLogarithmic.log10(im.maxval) - ValueAxisLogarithmic.log10(im.minval));
        }
    }

    int xtr(double mytime) {
        return (int) ((double) im.xorigin + pixieX * (mytime - im.start));
    }

    Iterator<RobinIterator.RobinPoint> ytr(final RobinTimeSet timeset) {
        return new Iterator<RobinIterator.RobinPoint>() {
            RobinIterator.RobinPoint point = new RobinIterator.RobinPoint();
            Iterator<RobinIterator.RobinPoint> i = timeset.iterator();
            boolean goodpoint = false;
            
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public RobinIterator.RobinPoint next() {
                if(! goodpoint) {
                    point.value = ytr(i.next().value);
                    goodpoint = true;
                    return point;
                }
                else {
                    goodpoint = false;
                    return point; 
                }
            }

            @Override
            public void remove() {
                i.remove();
            }
        };
    }

    int ytr(double value) {
        double yval;
        if (!gdef.logarithmic) {
            yval = im.yorigin - pixieY * (value - im.minval) + 0.5;
        }
        else {
            if (value < im.minval) {
                yval = im.yorigin;
            }
            else {
                yval = im.yorigin - pixieY * (ValueAxisLogarithmic.log10(value) - ValueAxisLogarithmic.log10(im.minval)) + 0.5;
            }
        }
        if (!gdef.rigid) {
            return (int) yval;
        }
        else if ((int) yval > im.yorigin) {
            return im.yorigin + 2;
        }
        else if ((int) yval < im.yorigin - im.ysize) {
            return im.yorigin - im.ysize - 2;
        }
        else {
            return (int) yval;
        }
    }

}
