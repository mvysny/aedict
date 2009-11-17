/*
  Java Japanese-English dictionary/Kanjidic browser
  Copyright (C) 1997 Todd David Rudick

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */
package edu.arizona.cs.javadict;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

// Handwritten Kanji Recognizer
// use as a panel. The ActionListener will receive an action whose
// name is a string of 10 or less kanji with the best match first.

public class DrawPanel extends Panel {
	public List<List<Integer>> xstrokes = new ArrayList<List<Integer>>();
	public List<List<Integer>> ystrokes = new ArrayList<List<Integer>>();
	public List<Integer> curxvec = null;
	public List<Integer> curyvec = null;

	public KCanvas c = new KCanvas(this);
	static final int NUMKAN = 5;

	private <T> T last(List<? extends T> list) {
		return list.get(list.size() - 1);
	}

	/**
	 * Clears the kanji strokes.
	 */
	public void clear() {
		xstrokes.clear();
		ystrokes.clear();
		curxvec = null;
		curyvec = null;
		Rectangle r = c.getBounds();
		c.getGraphics().clearRect(0, 0, r.width, r.height);
		c.paint(c.getGraphics());
	}

	private BufferedReader getResource(final int strokes) throws IOException {
		final InputStream result = getClass().getClassLoader().getResourceAsStream("unistrok." + strokes);
		if (result == null) {
			return null;
		}
		return new BufferedReader(new InputStreamReader(result, "UTF-8"));
	}

	private void closeQuietly(final Closeable c) {
		try {
			c.close();
		} catch (Exception ex) {
			System.out.println("WARN: FAILED TO CLOSE " + ex);
		}
	}

	/**
	 * Performs analysis of the currently drawn kanji. Returns {@value #NUMKAN}
	 * best matches.
	 * 
	 * @return the best matches, ordered from best to worst match.
	 */
	public String analyzeKanji() throws IOException {
		int sc;
		List<Integer> minScores = new ArrayList<Integer>(); // sorted such that
															// the best is last
		List<String> minChars = new ArrayList<String>();
		String curk;
		final BufferedReader in = getResource(xstrokes.size());
		if (in == null) {
			// no kanjis with given stroke count, just return an empty string
			return "";
		}
		try {
			String line;
			while (true) {
				line = in.readLine();
				String goline = "";
				if (line == null) {
					int sz;
					sz = minChars.size();
					char[] kanj = new char[sz];
					int i;
					for (i = 0; i < sz; i++) { // reverse array
						String s;
						s = minChars.get(sz - i - 1);
						if (s.charAt(0) == '0')
							kanj[i] = '?';
						else {
							int index;
							index = s.indexOf(' ');
							if (index != -1)
								s = s.substring(0, index);
							try {
								int hexcode;
								hexcode = Integer.parseInt(s, 16);
								kanj[i] = (char) hexcode;
							} catch (Exception ez11) {
								kanj[i] = '?';
							}
						}
					}
					return new String(kanj);
				} else {
					if (line.length() == 0)
						continue;
					if (line.charAt(0) == '#')
						continue;
					int index;
					index = line.indexOf('|');
					if (index == -1)
						continue;
					curk = line.substring(0, index);
					line = line.substring(index + 1);
					String tokline;
					String argline;
					int tokindex = line.indexOf('|');
					if (tokindex != -1) {
						tokline = line.substring(0, tokindex);
						argline = line.substring(tokindex + 1);
					} else {
						argline = null;
						tokline = line;
					}
					StringTokenizer st = new StringTokenizer(tokline);
					if (st.countTokens() != xstrokes.size())
						continue;

					WhileLoop: while (st.hasMoreTokens()) {
						String tok = st.nextToken();
						int i;
						for (i = 0; i < tok.length(); i++) {
							switch (tok.charAt(i)) {
							case '2':
							case '1':
							case '3':
							case '4':
							case '6':
							case '7':
							case '8':
							case '9':
								goline = goline + tok.charAt(i);
								break;
							case 'b':
								goline = goline + "62";
								break;
							case 'c':
								goline = goline + "26";
								break;
							case 'x':
								goline = goline + "21";
								break;
							case 'y':
								goline = goline + "23";
								break;
							case '|':
								break WhileLoop;
							default:
								throw new IOException("unknown symbol in kanji database: "+line);
//								continue;
							}
						}
						goline = goline + " ";
					}
					int ns;
					if (minScores.size() < NUMKAN)
						ns = getScore(goline, 999999);
					else {
						int cutoff1, cutoff2;
						cutoff1 = minScores.get(0);
						cutoff2 = minScores.get(minScores.size() - 1) * 2;
						ns = getScore(goline, Math.min(cutoff1, cutoff2));
					}
					// if more tokens exist, apply filters
					if (argline != null) {
						st = new StringTokenizer(argline);
						while (st.hasMoreTokens()) {
							try {
								String tok = st.nextToken();
								int minindex;
								minindex = tok.indexOf("-");
								if (minindex == -1) {
									throw new IOException("bad filter");
//									continue;
								}
								String arg1, arg2;
								arg1 = tok.substring(0, minindex);
								arg2 = tok.substring(minindex + 1, tok.length());
								int arg1stroke, arg2stroke;
								arg1stroke = Integer.parseInt(arg1.substring(1));
								boolean must = (arg2.charAt(arg2.length() - 1) == '!');
								if (must)
									arg2stroke = Integer.parseInt(arg2.substring(1, arg2.length() - 1));
								else
									arg2stroke = Integer.parseInt(arg2.substring(1));

								List<Integer> stroke1x, stroke1y, stroke2x, stroke2y;
								stroke1x = xstrokes.get(arg1stroke - 1);
								stroke1y = ystrokes.get(arg1stroke - 1);
								stroke2x = xstrokes.get(arg2stroke - 1);
								stroke2y = ystrokes.get(arg2stroke - 1);

								int val1, val2;
								switch (arg1.charAt(0)) {
								case 'x':
									val1 = stroke1x.get(0);
									break;
								case 'y':
									val1 = stroke1y.get(0);
									break;
								case 'i':
									val1 = stroke1x.get(stroke1x.size() - 1);
									break;
								case 'j':
									val1 = stroke1y.get(stroke1y.size() - 1);
									break;
								case 'a':
									val1 = ((stroke1x.get(0)) + (stroke1x.get(stroke1x.size() - 1))) / 2;
									break;
								case 'b':
									val1 = ((stroke1y.get(0)) + (stroke1y.get(stroke1y.size() - 1))) / 2;
									break;
								case 'l':
									int dx,
									dy;
									dx = (last(stroke1x)) - (stroke1x.get(0));
									dy = (last(stroke1y)) - (stroke1y.get(0));
									val1 = (int) (Math.sqrt((double) (dx * dx + dy * dy)));
									break;
								default:
									throw new IOException("bad filter");
//									continue;
								}
								// now the same thing for arg2 & val2
								switch (arg2.charAt(0)) {
								case 'x':
									val2 = (stroke2x.get(0));
									break;
								case 'y':
									val2 = (stroke2y.get(0));
									break;
								case 'i':
									val2 = (last(stroke2x));
									break;
								case 'j':
									val2 = (last(stroke2y));
									break;
								case 'a':
									val2 = ((stroke2x.get(0)) + (last(stroke2x))) / 2;
									break;
								case 'b':
									val2 = ((stroke2y.get(0)) + (last(stroke2y))) / 2;
									break;
								case 'l':
									int dx,
									dy;
									dx = (last(stroke2x)) - (stroke2x.get(0));
									dy = (last(stroke2y)) - (stroke2y.get(0));
									val2 = (int) (Math.sqrt((double) (dx * dx + dy * dy)));
									break;
								default:
									throw new IOException("bad filter");
//									continue;
								}
								// so now val1 and val2 have the right values
								ns = ns - (val1 - val2);
								if (must && (val1 < val2))
									ns += 9999999;
							} catch (Exception ez2) {
								throw new RuntimeException("bad filter", ez2);
//								continue;
							} // try-catch
						} // while
					}
					// now ns == the score
					int size;
					size = minScores.size();
					if ((size < NUMKAN) || (ns < minScores.get(0))) {
						if (size == 0) {
							minScores.add(ns);
							minChars.add(curk);
						} else {
							if (ns <= (last(minScores))) {
								minScores.add(new Integer(ns));
								minChars.add(curk);
							} else {
								int i = 0;
								while ((minScores.get(i)) > ns)
									i++;
								minScores.add(i, ns);
								minChars.add(i, curk);
							}
						}
					}
					size = minScores.size();
					if (size > NUMKAN) {
						minScores.remove(0);
						minChars.remove(0);
					}
				}
			}

		} finally {
			closeQuietly(in);
		}
	}

	// static final int HASHSIZE = 3000;
	static final int angScale = 1000;
	static final int sCost = (int) Math.round(Math.PI / 60.0 * angScale);
	static final int hugeCost = ((int) Math.round(Math.PI * angScale) + sCost) * 100;

	// endi is exclusive, begi inclusive
	int scoreStroke(List<Integer> xv, List<Integer> yv, int begi, int endi, String dir, int depth) {

		/*
		 * if (endi-1<=begi) { System.out.println("Ouch1"); return(hugeCost); }
		 */
		if (dir.length() == 1) {
			int i;
			int difx, dify;
			difx = (xv.get(endi - 1)) - (xv.get(begi));
			dify = (yv.get(endi - 1)) - (yv.get(begi));
			if ((difx == 0) && (dify == 0)) {
				// System.out.println("Ouch2");
				// return((int)Math.round(Math.PI*angScale)+sCost);
				return (hugeCost);
			}
			if ((difx * difx + dify * dify > (20 * 20)) && (endi - begi > 5) && (depth < 4)) {
				int mi = (endi + begi) / 2;
				int cost1, cost2;
				cost1 = scoreStroke(xv, yv, begi, mi, dir, depth + 1);
				cost2 = scoreStroke(xv, yv, mi, endi, dir, depth + 1);
				// return the average cost of the substrokes, but penalize if
				// they're
				// different.
				return ((cost1 + cost2) / 2);// +Math.abs(cost1-cost2));
			}

			double ang;
			ang = Math.atan2(-dify, difx);
			double myang;
			switch (dir.charAt(0)) {
			case '6':
				myang = 0;
				break;
			case '9':
				myang = Math.PI / 4;
				break;
			case '8':
				myang = Math.PI / 2;
				break;
			case '7':
				myang = Math.PI * 3 / 4;
				break;
			case '4':
				myang = Math.PI;
				break;
			case '3':
				myang = -Math.PI / 4;
				break;
			case '2':
				myang = -Math.PI / 2;
				break;
			case '1':
				myang = -Math.PI * 3 / 4;
				break;
			default:
				throw new RuntimeException("Illegal char: "+dir.charAt(0));
//				myang = 0;
			}
			double difang = myang - ang;
			while (difang < 0)
				difang += 2 * Math.PI;
			while (difang > 2 * Math.PI)
				difang -= 2 * Math.PI;
			if (difang > Math.PI)
				difang = 2 * Math.PI - difang;

			int retcost = (int) Math.round(difang * angScale) + sCost;
			return (retcost);
		} else if (begi == endi) {
			/*
			 * int i; int cost=0; for (i=0;i<dir.length();i++) {
			 * cost+=scoreStroke(xv,yv,begi,endi,dir.charAt(i)+""); } int
			 * retcost = cost/dir.length(); return(retcost);
			 */
			return (hugeCost * dir.length());
		} else { // recurse
			int l1, l2;
			l1 = dir.length() / 2;
			l2 = dir.length() - l1;
			String s1, s2;
			s1 = dir.substring(0, l1);
			s2 = dir.substring(l1, dir.length());
			int i;
			int mincost = hugeCost * dir.length() * 2;
			// 9999991;
			int s1l = s1.length();
			int s2l = s2.length();
			int step = (endi - begi) / 10;
			if (step < 1)
				step = 1;

			for (i = begi + 1 + s1l; i < endi - 1 - s2l; i += step) {
				int ncost;
				ncost = scoreStroke(xv, yv, begi, i + 1, s1, depth) + scoreStroke(xv, yv, i - 1, endi, s2, depth);
				if (ncost < mincost)
					mincost = ncost;
			}
			// if (mincost==hugeCost*dir.length()*2)
			// System.out.println("Ouch3");
			return (mincost);
		}
	}

	private int getScore(String s, int cutoff) { // , Hashtable h, int cutoff) {
		double score = 0;
		int strokes = 0;
		int maxscore = 0;
		cutoff = cutoff * xstrokes.size(); // need it before the averaging
		StringTokenizer st = new StringTokenizer(s);
		Iterator<List<Integer>> xe = xstrokes.iterator();
		Iterator<List<Integer>> ye = ystrokes.iterator();
		while (st.hasMoreTokens())
			if (!xe.hasNext())
				return (99997);
			else {
				/*
				 * if ((score>cutoff)&&(strokes>0)) { return(score/strokes); }
				 */
				List<Integer> vxe = xe.next();
				List<Integer> vye = ye.next();
				int thisscore;
				thisscore = scoreStroke(vxe, vye, 0, vxe.size(), st.nextToken(), 0);

				score = score + thisscore * thisscore;
				maxscore = Math.max(maxscore, thisscore);
				strokes++;
			}
		if (xe.hasNext())
			return (99998);
		else {
			if (strokes == 0)
				return (99997);
			else
				return ((int) Math.round(Math.sqrt(score)));
			// return(score/strokes+maxscore);

			/*
			 * count the worst stroke every time (sort of a pseudo-fuzzy type
			 * alg.)
			 */
		}
	}

	private static class KCanvas extends Canvas implements MouseMotionListener, MouseListener {
		DrawPanel paps;
		int lastx, lasty;
		static Color bg = new Color(30, 30, 50);
		static Color fg1 = new Color(235, 255, 235);
		static Color fg2 = new Color(160, 160, 255);

		public KCanvas(DrawPanel paps) {
			this.paps = paps;
			addMouseMotionListener(this);
			addMouseListener(this);
			setBackground(bg);
			setForeground(fg1);
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			if (paps.curxvec != null) {
				Graphics g = getGraphics();
				g.setColor(fg2);
				drawVec(g, paps.curxvec.iterator(), paps.curyvec.iterator());
			}

			paps.curxvec = new ArrayList<Integer>();
			paps.curyvec = new ArrayList<Integer>();
			paps.xstrokes.add(paps.curxvec);
			paps.ystrokes.add(paps.curyvec);
			lastx = e.getX();
			lasty = e.getY();
			paps.curxvec.add(lastx);
			paps.curyvec.add(lasty);
			// paint(getGraphics());
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseDragged(MouseEvent e) {
			int x, y;
			x = e.getX();
			y = e.getY();
			paps.curxvec.add(x);
			paps.curyvec.add(y);
			getGraphics().setColor(fg1);
			brushLine(getGraphics(), lastx, lasty, x, y);
			lastx = x;
			lasty = y;
		}

		public void brushLine(Graphics g, int x1, int y1, int x2, int y2) {
			g.drawLine(x1, y1, x2, y2);
		}

		public void mouseMoved(MouseEvent e) {
		}

		public void drawVec(Graphics g, Iterator<Integer> xe2, Iterator<Integer> ye2) {
			int lastx, lasty;
			lastx = -1;
			lasty = -1;
			while (xe2.hasNext()) {
				int x, y;
				x = xe2.next();
				y = ye2.next();
				if (lastx != -1)
					brushLine(g, lastx, lasty, x, y);
				lastx = x;
				lasty = y;
			} // while xe2
		}

		public void paint(Graphics g) {
			g.setColor(fg1);
			Rectangle r = getBounds();
			g.draw3DRect(0, 0, r.width, r.height, true);
			Iterator<List<Integer>> xe = paps.xstrokes.iterator();
			Iterator<List<Integer>> ye = paps.ystrokes.iterator();
			while (xe.hasNext()) {
				List<Integer> xvec, yvec;
				xvec = xe.next();
				yvec = ye.next();
				Iterator<Integer> xe2 = xvec.iterator();
				Iterator<Integer> ye2 = yvec.iterator();
				if (xvec != paps.curxvec)
					g.setColor(fg2);
				else
					g.setColor(fg1);
				drawVec(g, xe2, ye2);
			} // while xe
		}
	}

	public DrawPanel() {
		super();
		setLayout(new BorderLayout());
		c.setSize(100, 100);
		add(c, "Center");
		c.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		// Button b = new Button("Clear");
		// add(b);
		// b.addActionListener(this);
		// b.setActionCommand("Clear");
	}

	public static void main(String[] args) {
		Frame f = new Frame("Test");
		final DrawPanel dp = new DrawPanel();
		f.add(dp, "Center");
		Panel lower = new Panel();
		f.add(lower, "South");

		Button testb = new Button("Lookup");
		lower.add(testb);
		testb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println(dp.analyzeKanji());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		});
		testb.setActionCommand("Lookup");

		Button testb2 = new Button("Clear");
		lower.add(testb2);
		testb2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dp.clear();
			}

		});
		testb2.setActionCommand("Clear");

		f.pack();
		f.show();
	}
}
