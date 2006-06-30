﻿/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ************************************************************************
 */

import mx.controls.*
import mx.utils.*
import mx.managers.*
import mx.events.*

import org.lamsfoundation.lams.learner.*
import org.lamsfoundation.lams.common.Sequence
import org.lamsfoundation.lams.common.util.*
import org.lamsfoundation.lams.common.dict.*
import org.lamsfoundation.lams.common.style.*

class Header extends MovieClip {
	
	private var _header_mc:MovieClip;
	private var _container:MovieClip;		// Holding Container
	private var lams:MovieClip; 			// LAMS logo
	private var resume_btn:MovieClip;         //Resume and Exit buttons
    private var exit_btn:MovieClip;
	private var resume_lbl:TextField;
	private var exit_lbl:TextField;
	
	private var export_btn:Button;
	private var export_lbl:TextField;
	
	private var _lessonName:Label;
	
	private var panel:MovieClip;       //The underlaying panel base
    
	private var _tm:ThemeManager;
	private var _dictionary:Dictionary;
	
	//These are defined so that the compiler can 'see' the events that are added at runtime by EventDispatcher
    private var dispatchEvent:Function;     
    public var addEventListener:Function;
    public var removeEventListener:Function;
   
	
	/** 
	* constructor
	*/
	public function Header() {
		//Set up this class to use the Flash event delegation model
        EventDispatcher.initialize(this);
		
		_tm = ThemeManager.getInstance();
		_dictionary = Dictionary.getInstance();
		_dictionary.addEventListener('init',Proxy.create(this,setLabels));
		
		//Create a clip that will wait a frame before dispatching init to give components time to setup
        this.onEnterFrame = init;
	}
	
	/**
    * Called a frame after movie attached to allow components to initialise
    */
    public function init(){
		trace('initialing header..');
		
        //Delete the enterframe dispatcher
        delete this.onEnterFrame;
		
		_header_mc = this;
		setStyles();
		setLabels();
		resize(Stage.width);
		
		//get focus manager + set focus to OK button, focus manager is available to all components through getFocusManager
		//fm = _container.getFocusManager();
        //fm.enabled = true;
		
        //Add event listeners for resume and exit buttons
		
		resume_btn.onRelease = function(){
			trace('on releasing resuming button..');
			var app:Application = Application.getInstance();
			app.getLesson().resumeLesson();
		}
		
		exit_btn.onRelease = function(){
			trace('on releasing exit button..');
			var app:Application = Application.getInstance();
			app.getLesson().exitLesson();
		}
		
		export_btn.onRelease = function(){
			var app:Application = Application.getInstance();
			app.getLesson().exportLesson();
		}
		
        dispatchEvent({type:'load',target:this});
        
	}
	
	private function setStyles(){
		var styleObj = _tm.getStyleObject('smallLabel');
		_lessonName.setStyle('styleName', styleObj);
	}
	
	private function setLabels(){
		//Set the text for buttons
        resume_lbl.text = Dictionary.getValue('hd_resume_lbl');
        exit_lbl.text = Dictionary.getValue('hd_exit_lbl');
		export_lbl.text = Dictionary.getValue('ln_export_btn');
	}
	
	public function setLessonName(lessonName:String){
		_lessonName.text = lessonName;
	}
	
	public function resize(width:Number){
		panel._width = width;
		
	}
	
	function get className():String { 
        return 'Header';
    }
}