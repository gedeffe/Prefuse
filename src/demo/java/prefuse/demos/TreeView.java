package prefuse.demos;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.TreeDepthItemSorter;

/**
 * Demonstration of a node-link tree viewer
 *
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class TreeView extends Display {

	public static final String TREE_CHI = "/chi-ontology.xml.gz";

	private static final String tree = "tree";
	public static final String treeNodes = "tree.nodes";
	private static final String treeEdges = "tree.edges";

	private final LabelRenderer m_nodeRenderer;
	private final EdgeRenderer m_edgeRenderer;

	private String m_label = "label";
	private int m_orientation = Constants.ORIENT_LEFT_RIGHT;

	public TreeView(final Tree t, final String label) {
		super(new Visualization());
		this.m_label = label;

		this.m_vis.add(tree, t);

		this.m_nodeRenderer = new LabelRenderer(this.m_label);
		this.m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
		this.m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
		this.m_nodeRenderer.setRoundedCorner(8, 8);
		this.m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_CURVE);

		final DefaultRendererFactory rf = new DefaultRendererFactory(this.m_nodeRenderer);
		rf.add(new InGroupPredicate(treeEdges), this.m_edgeRenderer);
		this.m_vis.setRendererFactory(rf);

		// colors
		final ItemAction nodeColor = new NodeColorAction(treeNodes);
		final ItemAction textColor = new ColorAction(treeNodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0));
		this.m_vis.putAction("textColor", textColor);

		final ItemAction edgeColor = new ColorAction(treeEdges, VisualItem.STROKECOLOR, ColorLib.rgb(200, 200, 200));

		// quick repaint
		final ActionList repaint = new ActionList();
		repaint.add(nodeColor);
		repaint.add(new RepaintAction());
		this.m_vis.putAction("repaint", repaint);

		// full paint
		final ActionList fullPaint = new ActionList();
		fullPaint.add(nodeColor);
		this.m_vis.putAction("fullPaint", fullPaint);

		// animate paint change
		final ActionList animatePaint = new ActionList(400);
		animatePaint.add(new ColorAnimator(treeNodes));
		animatePaint.add(new RepaintAction());
		this.m_vis.putAction("animatePaint", animatePaint);

		// create the tree layout action
		final NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(tree, this.m_orientation, 50, 0, 8);
		treeLayout.setLayoutAnchor(new Point2D(25, 300));
		this.m_vis.putAction("treeLayout", treeLayout);

		final CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(tree, this.m_orientation);
		this.m_vis.putAction("subLayout", subLayout);

		final AutoPanAction autoPan = new AutoPanAction();

		// create the filtering and layout
		final ActionList filter = new ActionList();
		filter.add(new FisheyeTreeFilter(tree, 2));
		filter.add(new FontAction(treeNodes, FontLib.getFont("Tahoma", 16)));
		filter.add(treeLayout);
		filter.add(subLayout);
		filter.add(textColor);
		filter.add(nodeColor);
		filter.add(edgeColor);
		this.m_vis.putAction("filter", filter);

		// animated transition
		final ActionList animate = new ActionList(1000);
		animate.setPacingFunction(new SlowInSlowOutPacer());
		animate.add(autoPan);
		animate.add(new QualityControlAnimator());
		animate.add(new VisibilityAnimator(tree));
		animate.add(new LocationAnimator(treeNodes));
		animate.add(new ColorAnimator(treeNodes));
		animate.add(new RepaintAction());
		this.m_vis.putAction("animate", animate);
		this.m_vis.alwaysRunAfter("filter", "animate");

		// create animator for orientation changes
		final ActionList orient = new ActionList(2000);
		orient.setPacingFunction(new SlowInSlowOutPacer());
		orient.add(autoPan);
		orient.add(new QualityControlAnimator());
		orient.add(new LocationAnimator(treeNodes));
		orient.add(new RepaintAction());
		this.m_vis.putAction("orient", orient);

		// ------------------------------------------------

		// initialize the display
		this.setSize(700, 600);
		this.setItemSorter(new TreeDepthItemSorter());
		this.addControlListener(new ZoomToFitControl());
		this.addControlListener(new ZoomControl());
		this.addControlListener(new WheelZoomControl());
		this.addControlListener(new PanControl());
		this.addControlListener(new FocusControl(1, "filter"));

		this.setOnKeyTyped(new OrientAction(Constants.ORIENT_LEFT_RIGHT, KeyCode.DIGIT1));
		this.setOnKeyTyped(new OrientAction(Constants.ORIENT_TOP_BOTTOM, KeyCode.DIGIT2));
		this.setOnKeyTyped(new OrientAction(Constants.ORIENT_RIGHT_LEFT, KeyCode.DIGIT3));
		this.setOnKeyTyped(new OrientAction(Constants.ORIENT_BOTTOM_TOP, KeyCode.DIGIT4));

		// ------------------------------------------------

		// filter graph and perform layout
		this.setOrientation(this.m_orientation);
		this.m_vis.run("filter");

		final TupleSet search = new PrefixSearchTupleSet();
		this.m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
		search.addTupleSetListener(new TupleSetListener() {
			@Override
			public void tupleSetChanged(final TupleSet t, final Tuple[] add, final Tuple[] rem) {
				TreeView.this.m_vis.cancel("animatePaint");
				TreeView.this.m_vis.run("fullPaint");
				TreeView.this.m_vis.run("animatePaint");
			}
		});
	}

	// ------------------------------------------------------------------------

	public void setOrientation(final int orientation) {
		final NodeLinkTreeLayout rtl = (NodeLinkTreeLayout) this.m_vis.getAction("treeLayout");
		final CollapsedSubtreeLayout stl = (CollapsedSubtreeLayout) this.m_vis.getAction("subLayout");
		switch (orientation) {
		case Constants.ORIENT_LEFT_RIGHT:
			this.m_nodeRenderer.setHorizontalAlignment(Constants.LEFT);
			this.m_edgeRenderer.setHorizontalAlignment1(Constants.RIGHT);
			this.m_edgeRenderer.setHorizontalAlignment2(Constants.LEFT);
			this.m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
			this.m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
			break;
		case Constants.ORIENT_RIGHT_LEFT:
			this.m_nodeRenderer.setHorizontalAlignment(Constants.RIGHT);
			this.m_edgeRenderer.setHorizontalAlignment1(Constants.LEFT);
			this.m_edgeRenderer.setHorizontalAlignment2(Constants.RIGHT);
			this.m_edgeRenderer.setVerticalAlignment1(Constants.CENTER);
			this.m_edgeRenderer.setVerticalAlignment2(Constants.CENTER);
			break;
		case Constants.ORIENT_TOP_BOTTOM:
			this.m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
			this.m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
			this.m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
			this.m_edgeRenderer.setVerticalAlignment1(Constants.BOTTOM);
			this.m_edgeRenderer.setVerticalAlignment2(Constants.TOP);
			break;
		case Constants.ORIENT_BOTTOM_TOP:
			this.m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
			this.m_edgeRenderer.setHorizontalAlignment1(Constants.CENTER);
			this.m_edgeRenderer.setHorizontalAlignment2(Constants.CENTER);
			this.m_edgeRenderer.setVerticalAlignment1(Constants.TOP);
			this.m_edgeRenderer.setVerticalAlignment2(Constants.BOTTOM);
			break;
		default:
			throw new IllegalArgumentException("Unrecognized orientation value: " + orientation);
		}
		this.m_orientation = orientation;
		rtl.setOrientation(orientation);
		stl.setOrientation(orientation);
	}

	public int getOrientation() {
		return this.m_orientation;
	}

	// ------------------------------------------------------------------------

	public class OrientAction implements EventHandler<KeyEvent> {
		private final int orientation;
		private final KeyCode keyCode;

		public OrientAction(final int orientation, final KeyCode keyCodeParam) {
			this.orientation = orientation;
			this.keyCode = keyCodeParam;
		}

		@Override
		public void handle(final KeyEvent keyEvent) {
			if (keyEvent.isControlDown() && keyEvent.getCode().equals(this.keyCode)) {
				TreeView.this.setOrientation(this.orientation);
				TreeView.this.getVisualization().cancel("orient");
				TreeView.this.getVisualization().run("treeLayout");
				TreeView.this.getVisualization().run("orient");
			}
		}
	}

	public class AutoPanAction extends Action {
		private final Point2D m_start = Point2D.ZERO;
		private Point2D m_end = Point2D.ZERO;
		private Point2D m_cur = Point2D.ZERO;
		private final int m_bias = 150;

		@Override
		public void run(final double frac) {
			final TupleSet ts = this.m_vis.getFocusGroup(Visualization.FOCUS_ITEMS);
			if (ts.getTupleCount() == 0) {
				return;
			}

			if (frac == 0.0) {
				int xbias = 0, ybias = 0;
				switch (TreeView.this.m_orientation) {
				case Constants.ORIENT_LEFT_RIGHT:
					xbias = this.m_bias;
					break;
				case Constants.ORIENT_RIGHT_LEFT:
					xbias = -this.m_bias;
					break;
				case Constants.ORIENT_TOP_BOTTOM:
					ybias = this.m_bias;
					break;
				case Constants.ORIENT_BOTTOM_TOP:
					ybias = -this.m_bias;
					break;
				}

				final VisualItem vi = (VisualItem) ts.tuples().next();
				this.m_cur = new Point2D(TreeView.this.getWidth() / 2, TreeView.this.getHeight() / 2);
				this.m_cur = TreeView.this.getAbsoluteCoordinate(this.m_cur);
				this.m_end = new Point2D(vi.getX() + xbias, vi.getY() + ybias);
			} else {
				this.m_cur = new Point2D(this.m_start.getX() + (frac * (this.m_end.getX() - this.m_start.getX())),
						this.m_start.getY() + (frac * (this.m_end.getY() - this.m_start.getY())));
				TreeView.this.panToAbs(this.m_cur);
			}
		}
	}

	public static class NodeColorAction extends ColorAction {

		public NodeColorAction(final String group) {
			super(group, VisualItem.FILLCOLOR);
		}

		@Override
		public int getColor(final VisualItem item) {
			if (this.m_vis.isInGroup(item, Visualization.SEARCH_ITEMS)) {
				return ColorLib.rgb(255, 190, 190);
			} else if (this.m_vis.isInGroup(item, Visualization.FOCUS_ITEMS)) {
				return ColorLib.rgb(198, 229, 229);
			} else if (item.getDOI() > -1) {
				return ColorLib.rgb(164, 193, 193);
			} else {
				return ColorLib.rgba(255, 255, 255, 0);
			}
		}

	} // end of inner class TreeMapColorAction

} // end of class TreeMap
