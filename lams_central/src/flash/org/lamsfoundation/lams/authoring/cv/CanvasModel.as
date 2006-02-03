﻿import org.lamsfoundation.lams.common.*;
import org.lamsfoundation.lams.authoring.cv.*;
import org.lamsfoundation.lams.authoring.*;
import org.lamsfoundation.lams.common.util.*;
import mx.events.*
/*
* Model for the Canvas
*/
class org.lamsfoundation.lams.authoring.cv.CanvasModel extends Observable {
	
	public static var TRANSITION_TOOL:String = "TRANSITION";
	public static var OPTIONAL_TOOL:String = "OPTIONAL";
	
	private var __width:Number;
	private var __height:Number;
	private var __x:Number;
	private var __y:Number;
	
	private var infoObj:Object;
	
	
	private var _cv:Canvas;
		//UI State variabls	private var _isDirty:Boolean;
	private var _activeTool:String;
	private var _selectedItem:Object;  // the currently selected thing - could be activity, transition etc.
	private var _isDrawingTransition:Boolean;
	private var _transitionActivities:Array;
	private var _isDragging:Boolean;
	

	
	//these are hashtables of mc refs MOVIECLIPS (like CanvasActivity or CanvasTransition)
	//each on contains a reference to the emelment in the ddm (activity or transition)
	private var _activitiesDisplayed:Hashtable;
	private var _transitionsDisplayed:Hashtable;
	//These are defined so that the compiler can 'see' the events that are added at runtime by EventDispatcher
    private var dispatchEvent:Function;     
    public var addEventListener:Function;
    public var removeEventListener:Function;
	
	/**
	* Constructor.
	*/
	public function CanvasModel (cv:Canvas){
		
		 //Set up this class to use the Flash event delegation model
        EventDispatcher.initialize(this);
		_cv = cv;
		_activitiesDisplayed = new Hashtable("_activitiesDisplayed");
		_transitionsDisplayed = new Hashtable("_transitionsDisplayed");
		
		
		
				
	
		_activeTool = null;
		_transitionActivities = new Array();
	}

	
	/**
	* Used by application to set the size
	* @param width The desired width
	* @param height the desired height
	*/
	public function setSize(width:Number, height:Number):Void{
		__width = width;
		__height = height;
		
		/*
		//send an update
		setChanged();
		infoObj = {};
		infoObj.updateType = "SIZE";
		notifyObservers(infoObj);
		*/
		broadcastViewUpdate("SIZE");
		
	}
	
	/**
	* Used by View to get the size
	* @returns Object containing width(w) & height(h).  obj.w & obj.h
	*/
	public function getSize():Object{
		var s:Object = {};
		s.w = __width;
		s.h = __height;
		return s;
	}
	
	
	
	/**
	* Used by application to set the Position
	* @param x
	* @param y
	*/
	public function setPosition(x:Number, y:Number):Void{
		__x=x;
		__y=y;
		/*
		//send an update
		setChanged();
		infoObj = {};
		infoObj.updateType = "POSITION";
		notifyObservers(infoObj);
		*/
		broadcastViewUpdate("POSITION");
	}
	
	
	
	/**
	* Used by View to get the size
	* @returns Object containing width(w) & height(h).  obj.w & obj.h
	*/
	public function getPosition():Object{
		var p:Object = {};
		p.x = __x;
		p.y = __y;
		return p;
	}

	public function setDirty(){
		_isDirty = true;
		/*
		//work out what we need to redraw.
		//for now lets just do a full re-draw
		//send an update
		setChanged();
		infoObj = {};
		infoObj.updateType = "DRAW_DESIGN";
		notifyObservers(infoObj);
		*/
		refreshDesign();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////       TRANSITIONS         //////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Starts the transition tool
	 * @usage   
	 * @return  
	 */
	public function startTransitionTool():Void{
		Debugger.log('Starting transition tool',Debugger.GEN,'startTransitionTool','CanvasModel');			
		resetTransitionTool();
		_activeTool = CanvasModel.TRANSITION_TOOL;
		broadcastViewUpdate("START_TRANSITION_TOOL");
	}
	
	/**
	 * Stops it
	 * @usage   
	 * @return  
	 */
	 
	public function stopTransitionTool():Void{
		Debugger.log('Stopping transition tool',Debugger.GEN,'stopTransitionTool','CanvasModel');
		resetTransitionTool();
		_activeTool = null;
		broadcastViewUpdate("STOP_TRANSITION_TOOL");
	}
	
	/**
	 * Adds another Canvas Activity to the transition.  
	 * Only 2 may be added, adding the 2nd one triggers the creation of the transition.
	 * @usage   
	 * @param   ca (Canvas Activity)
	 * @return  
	 */
	public function addActivityToTransition(ca:Object):Object{
		//check we have not added too many
		
		if(_transitionActivities.length >= 2){
			//TODO: show an error
			return new LFError("Too many activities in the Transition","addActivityToTransition",this);
		}
		Debugger.log('Adding Activity.UIID:'+ca.activity.activityUIID,Debugger.GEN,'addActivityToTransition','CanvasModel');
		_transitionActivities.push(ca);
		
		if(_transitionActivities.length == 2){
			//check we have 2 valid acts to create the transition.
			if(_transitionActivities[0].activity.activityUIID == _transitionActivities[1].activity.activityUIID){
				return new LFError("You cannot create a Transition between the same Activities","addActivityToTransition",this);
			}
			if(!_activitiesDisplayed.containsKey(_transitionActivities[0].activity.activityUIID)){
				return new LFError("First activity of the Transition is missing, UIID:"+_transitionActivities[0].activity.activityUIID,"addActivityToTransition",this);
			}
			if(!_activitiesDisplayed.containsKey(_transitionActivities[1].activity.activityUIID)){
				return new LFError("Second activity of the Transition is missing, UIID:"+_transitionActivities[1].activity.activityUIID,"addActivityToTransition",this);
			}
			
			//check there is not already a transition to or from this activity:
			var transitionsArray:Array = _cv.ddm.transitions.values();
			/**/
			for(var i=0;i<transitionsArray.length;i++){
				
				if(transitionsArray[i].toUIID == _transitionActivities[1].activity.activityUIID){
					return new LFError("A Transition to Activity '"+_transitionActivities[1].activity.title+"' already exists","addActivityToTransition",this,'activityUIID:'+_transitionActivities[1].activity.activityUIID);
				}
				
				if(transitionsArray[i].fromUIID == _transitionActivities[0].activity.activityUIID){
					return new LFError("A Transition from Activity '"+_transitionActivities[0].activity.title+"' already exists","addActivityToTransition",this,'activityUIID:'+_transitionActivities[1].activity.activityUIID);
				}
			}
			
			//TODO: Check for loop
			//TODO: Check for activity inside optional
			
			
			Debugger.log('No validation errors, creating transition.......',Debugger.GEN,'addActivityToTransition','CanvasModel');
			//lets make the transition
			var t:Transition = createTransition(_transitionActivities);
			
			
			//add it to the DDM
			var success:Object = _cv.ddm.addTransition(t);
			//flag the model as dirty and trigger a refresh
			setDirty();
			
			_cv.stopTransitionTool();
			
		}
		
		
		
		
		return true;
	}
	
	/**
	 * Resets the transition tool to its starting state, e.g. if one chas been created or the user released the mouse over an unsuitable clip
	 * @usage   
	 */
	public function resetTransitionTool():Void{
		//clear the transitions array
			_transitionActivities = new Array();
	}
	
	public function isTransitionToolActive():Boolean{
	   if(_activeTool == TRANSITION_TOOL){
		   return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Forms a transition	
	 * @usage   
	 * @param   transitionActs An array of transition activities. Must only contain 2
	 * @return  
	 */
	private function createTransition(transitionActs:Array):Transition{
		var fromAct:Activity = transitionActs[0].activity;
		var toAct:Activity = transitionActs[1].activity;
		
		var t:Transition = new Transition(_cv.ddm.newUIID(),fromAct.activityUIID,toAct.activityUIID,_cv.ddm.learningDesignID);
		
		return t;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////       REFRESHING DESIGNS       /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * Compares 2 activities, decides if they are new, the same or to be deleted
	 * @usage   
	 * @param   ddm_activity Design Data Model activity
	 * @param   cm_activity  Canvas Model activity
	 * @return  
	 */
	private function compareActivities(ddm_activity:Activity,cm_activity:Activity):Object{
		Debugger.log('Comparing ddm_activity:'+ddm_activity.title+'('+ddm_activity.activityUIID+') WITH cm_activity:'+cm_activity.title+'('+cm_activity.activityUIID+')',Debugger.GEN,'compareActivities','CanvasModel');
		var r:Object = new Object();
		
		//check if the activity has a parent, if so then we dont need to check it
		Debugger.log('Checking parent activity IDs, parentUIID:'+ddm_activity.parentUIID+'parentID:'+ddm_activity.parentActivityID,Debugger.GEN,'refreshDesign','CanvasModel');
		if(ddm_activity.parentActivityID > 0 || ddm_activity.parentUIID > 0){
			return r = "CHILD";
		}
		
		//if they are the same (ref should point to same act) then nothing to do.
		//if the ddm does not have an act displayed then we need to remove it from the cm
		//if the ddm has an act that cm does not ref, then we need to add it.
			
		if(ddm_activity === cm_activity){
			return r = "SAME";
		}
		
		//check for a new act in the dmm
		if(cm_activity == null || cm_activity == undefined){
			return r = "NEW";
		}
		
		//check if act has been removed from canvas
		if(ddm_activity == null || ddm_activity == undefined){
			return r = "DELETE";
		}
		
			
	}
	
	/**
	 * Compares 2 transitions, decides if they are new, the same or to be deleted
	 * @usage   
	 * @param   ddm_transition 
	 * @param   cm_transition  
	 * @return  
	 */
	private function compareTransitions(ddm_transition:Transition, cm_transition:Transition):Object{
		Debugger.log('Comparing ddm_activity:'+ddm_transition.title+'('+ddm_transition.transitionUIID+') WITH cm_transition:'+cm_transition.title+'('+cm_transition.transitionUIID+')',Debugger.GEN,'compareTransitions','CanvasModel');
		var r:Object = new Object();
		if(ddm_transition === cm_transition){
			return r = "SAME";
		}
		
		//check for a new act in the dmm
		if(cm_transition == null){
			return r = "NEW";
		}
		
		//check if act has been removed from canvas
		if(ddm_transition == null){
			return r = "DELETE";
		}
		
		
	}
	
	/**
	 * Compares the design in the CanvasModel (what is displayed on the screen) 
	 * against the design in the DesignDataModel and updates the Canvas Model accordingly.
	 * NOTE: Design elements are added to the DDM here, but removed in the View
	 * 
	 * @usage   
	 * @return  
	 */
	private function refreshDesign(){
	
		//porobbably need to get a bit more granular		Debugger.log('Running',Debugger.GEN,'refreshDesign','CanvasModel');
		//go through the design and see what has changed, compare DDM to canvasModel
		var ddmActivity_keys:Array = _cv.ddm.activities.keys();
		Debugger.log('ddmActivity_keys.length:'+ddmActivity_keys.length,Debugger.GEN,'refreshDesign','CanvasModel');
		//Debugger.log('ddmActivity_keys::'+ddmActivity_keys.toString(),Debugger.GEN,'refreshDesign','CanvasModel');
		var cmActivity_keys:Array = _activitiesDisplayed.keys();
		Debugger.log('cmActivity_keys.length:'+cmActivity_keys.length,Debugger.GEN,'refreshDesign','CanvasModel');
		//Debugger.log('cmActivity_keys:'+cmActivity_keys.toString(),Debugger.GEN,'refreshDesign','CanvasModel');
		
		
		
		var longest = Math.max(ddmActivity_keys.length, cmActivity_keys.length);
		
		//chose which array we are going to loop over
		var indexArray:Array;
		
		if(ddmActivity_keys.length == longest){
			indexArray = ddmActivity_keys;
		}else{
			indexArray = cmActivity_keys;
		}
		
		
		//loop through and do comparison
		for(var i=0;i<longest;i++){
			//check DDM against CM, DDM is king.
			/*
			var keyToCheck:Number = ddmActivity_keys[i];
			// if its nan then we have to use the cm version
			if(isNaN(keyToCheck)){
				keyToCheck = cmActivity_keys[i];
			}
			*/
			
			var keyToCheck:Number = indexArray[i];
			
			
			var ddm_activity:Activity = _cv.ddm.activities.get(keyToCheck);
			var cm_activity:Activity = _activitiesDisplayed.get(keyToCheck).activity;			//if they are the same (ref should point to same act) then nothing to do.
			//if the ddm does not have an act displayed then we need to remove it from the cm
			//if the ddm has an act that cm does not ref, then we need to add it.
			
			
			var r_activity:Object = compareActivities(ddm_activity, cm_activity);
			
			Debugger.log('r_activity:'+r_activity,Debugger.GEN,'refreshDesign','CanvasModel');
			if(r_activity == "NEW"){
				//draw this activity
				//NOTE!: we are passing in a ref to the activity in the ddm so if we change any props of this, we are changing the ddm
				
				broadcastViewUpdate("DRAW_ACTIVITY",ddm_activity);
				
			}else if(r_activity == "DELETE"){
				//remove this activity
				if(cm_activity.parentUIID == null){
					broadcastViewUpdate("REMOVE_ACTIVITY",cm_activity);
				}
			}else if(r_activity == "CHILD"){
				//dont ask the view to draw the activity if it is a child act				
				Debugger.log('Found a child activity, not drawing. activityID:'+ddm_activity.activityID+'parentID:'+ddm_activity.parentActivityID,Debugger.GEN,'refreshDesign','CanvasModel');
				
				
			}else{
	
			}
		
			
		}
		
		//now check the transitions:
		var ddmTransition_keys:Array = _cv.ddm.transitions.keys();
		var cmTransition_keys:Array = _transitionsDisplayed.keys();
		var trLongest = Math.max(ddmTransition_keys.length, cmTransition_keys.length);
		//chose which array we are going to loop over
		var trIndexArray:Array;
		
		if(ddmTransition_keys.length == trLongest){
			trIndexArray = ddmTransition_keys;
		}else{
			trIndexArray = cmTransition_keys;
		}
		
		
		//loop through and do comparison
		for(var i=0;i<trIndexArray.length;i++){
			
			var transitionKeyToCheck:Number = trIndexArray[i];

			var ddmTransition:Transition = _cv.ddm.transitions.get(transitionKeyToCheck);
			var cmTransition:Transition = _transitionsDisplayed.get(transitionKeyToCheck).transition;
			
			var r_transition:Object = compareTransitions(ddmTransition, cmTransition);
			
			if(r_transition == "NEW"){
				//NOTE!: we are passing in a ref to the tns in the ddm so if we change any props of this, we are changing the ddm
				broadcastViewUpdate("DRAW_TRANSITION",ddmTransition);
			}else if(r_transition == "DELETE"){
				broadcastViewUpdate("REMOVE_TRANSITION",cmTransition);
			}
			
		}
		
		
		
		
		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////       EDITING ACTIVITIES               /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Called on double clicking an activity
	 * @usage   
	 * @return  
	 */
	public function openToolActivityContent(ta:ToolActivity):Void{
		Debugger.log('ta:'+ta.title+'toolContentID:'+ta.toolContentID,Debugger.GEN,'openToolActivityContent','CanvasModel');
		//check if we have a toolContentID
		var defaultContentID:Number = Application.getInstance().getToolkit().getDefaultContentID(ta.learningLibraryID,ta.toolID);
		Debugger.log('ta:'+ta.title+'toolContentID:'+ta.toolContentID+', default content ID:'+defaultContentID,Debugger.GEN,'openToolActivityContent','CanvasModel');
		if(ta.toolContentID == defaultContentID){
			getNewToolContentID(ta);
		}else{
		
			//if we have a good toolID lets open it
			if(ta.toolContentID > 0){
				var url:String;
				var cfg = Config.getInstance();
				if(ta.authoringURL.indexOf("?") != -1){
					//09-11-05 Change to toolContentID and remove userID.
					//url = cfg.serverUrl+ta.authoringURL + '&toolContentId='+ta.toolContentID+'&userID='+cfg.userID;
					url = cfg.serverUrl+ta.authoringURL + '&toolContentID='+ta.toolContentID;
				}else{
					//url = cfg.serverUrl+ta.authoringURL + '?toolContentId='+ta.toolContentID+'&userID='+cfg.userID;
					url = cfg.serverUrl+ta.authoringURL + '?toolContentID='+ta.toolContentID;
				}
			
				Debugger.log('Opening url:'+url,Debugger.GEN,'openToolActivityContent','CanvasModel');
				getURL(url,"_blank");
			
			
			}else{
				new LFError("We dont have a valid toolContentID","openToolActivityContent",this);
			}
		
		}
		
		
		
		
	}
	
	public function getNewToolContentID(ta:ToolActivity):Void{
		Debugger.log('ta:'+ta.title+', activityUIID:'+ta.activityUIID,Debugger.GEN,'getNewToolContentID','CanvasModel');
		var callback:Function = Proxy.create(this,setNewToolContentID,ta);
		Application.getInstance().getComms().getRequest('authoring/author.do?method=getToolContentID&toolID='+ta.toolID,callback, false);
	}
	
	public function setNewToolContentID(toolContentID:Number,ta:ToolActivity):Void{
		Debugger.log('new content ID from server:'+toolContentID,Debugger.GEN,'setNewToolContentID','CanvasModel');
		ta.toolContentID = toolContentID;
		Debugger.log('ta:'+ta.title+',toolContentID:'+ta.toolContentID+', activityUIID:'+ta.activityUIID,Debugger.GEN,'setNewToolContentID','CanvasModel');
		openToolActivityContent(ta);
	}
	
	
	
	/**
    * Notify registered listeners that a data model change has happened
    */
    public function broadcastViewUpdate(_updateType,_data){
        dispatchEvent({type:'viewUpdate',target:this,updateType:_updateType,data:_data});
        trace('broadcast');
    }
	
	
	
	/**
	 * Returns a reference to the Activity Movieclip for the UIID passed in.  Gets from _activitiesDisplayed Hashable
	 * @usage   
	 * @param   UIID 
	 * @return  Activity Movie clip
	 */
	public function getActivityMCByUIID(UIID:Number):MovieClip{
		
		var a_mc:MovieClip = _activitiesDisplayed.get(UIID);
		//Debugger.log('UIID:'+UIID+'='+a_mc,Debugger.GEN,'getActivityMCByUIID','CanvasModel');
		return a_mc;
	}
	/*
	public function setContainerRef(c:Canvas):Void{
		_cv = c;
	}
	
	*/
	//Getters n setters
	
	public function getCanvas():Canvas{
		return _cv;
	}
	
	public function get activitiesDisplayed():Hashtable{
		return _activitiesDisplayed;
	}
	
	public function get transitionsDisplayed():Hashtable{
		return _transitionsDisplayed;
	}
	
	public function get isDrawingTransition():Boolean{
		return _isDrawingTransition;
	}
	/**
	 * 
	 * @usage   
	 * @param   newactivetool 
	 * @return  
	 */
	public function set activeTool (newactivetool:String):Void {
		_activeTool = newactivetool;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get activeTool ():String {
		return _activeTool;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newselectItem 
	 * @return  
	 */
	public function set selectedItem (newselectItem:Object):Void {
		_selectedItem = newselectItem;
		broadcastViewUpdate("SELECTED_ITEM");
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get selectedItem ():Object {
		return _selectedItem;
	}
	
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get isDragging ():Boolean {
		return _isDragging;
	}
	
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function set isDragging (newisDragging:Boolean):Void{
		_isDragging = newisDragging;
	}
	
}
