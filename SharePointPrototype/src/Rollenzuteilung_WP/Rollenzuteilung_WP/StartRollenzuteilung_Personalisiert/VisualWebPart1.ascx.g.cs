﻿//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:4.0.30319.18051
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace Rollenzuteilung_WP.VisualWebPart1 {
    using System.Web.UI.WebControls.Expressions;
    using System.Web.UI.HtmlControls;
    using System.Collections;
    using System.Text;
    using System.Web.UI;
    using System.Collections.Generic;
    using System.Linq;
    using System.Xml.Linq;
    using Microsoft.SharePoint.WebPartPages;
    using System.Web.SessionState;
    using System.Configuration;
    using Microsoft.SharePoint;
    using System.Web;
    using System.Web.DynamicData;
    using System.Web.Caching;
    using System.Web.Profile;
    using System.ComponentModel.DataAnnotations;
    using System.Web.UI.WebControls;
    using System.Web.Security;
    using System;
    using Microsoft.SharePoint.Utilities;
    using System.Text.RegularExpressions;
    using System.Collections.Specialized;
    using System.Web.UI.WebControls.WebParts;
    using Microsoft.SharePoint.WebControls;
    
    
    public partial class VisualWebPart1 {
        
        protected global::System.Web.UI.WebControls.Button Button1;
        
        protected global::System.Web.UI.WebControls.Label Rollenzuteilung_erledigt;
        
        protected global::System.Web.UI.WebControls.Label Rollenzuteilung_failed;
        
        protected global::System.Web.UI.WebControls.Label Rollenzuteilung_failed_errormessage;
        
        public static implicit operator global::System.Web.UI.TemplateControl(VisualWebPart1 target) 
        {
            return target == null ? null : target.TemplateControl;
        }
        
        [global::System.ComponentModel.EditorBrowsableAttribute(global::System.ComponentModel.EditorBrowsableState.Never)]
        private global::System.Web.UI.WebControls.Button @__BuildControlButton1() {
            global::System.Web.UI.WebControls.Button @__ctrl;
            @__ctrl = new global::System.Web.UI.WebControls.Button();
            this.Button1 = @__ctrl;
            @__ctrl.ApplyStyleSheetSkin(this.Page);
            @__ctrl.ID = "Button1";
            @__ctrl.Text = "Der für die Rolle verantwortlichen Person die Aufgaben und Ergebnisse jetzt zuwei" +
                "sen ....";
            @__ctrl.ToolTip = "Nachdem Sie den Rollen die verantwortliche Person zugewiesen haben, können Sie de" +
                "n Aufgaben und Ergebnissen, die noch keine personalisierte Zuteilung aufweisen, " +
                "die entsprechende verantwortliche Person zuordnen.";
            @__ctrl.Click -= new System.EventHandler(this.Button1_Click);
            @__ctrl.Click += new System.EventHandler(this.Button1_Click);
            return @__ctrl;
        }
        
        [global::System.ComponentModel.EditorBrowsableAttribute(global::System.ComponentModel.EditorBrowsableState.Never)]
        private global::System.Web.UI.WebControls.Label @__BuildControlRollenzuteilung_erledigt() {
            global::System.Web.UI.WebControls.Label @__ctrl;
            @__ctrl = new global::System.Web.UI.WebControls.Label();
            this.Rollenzuteilung_erledigt = @__ctrl;
            @__ctrl.ApplyStyleSheetSkin(this.Page);
            @__ctrl.ID = "Rollenzuteilung_erledigt";
            @__ctrl.Text = "Die personalisierte Rollenzuteilung wurde aktualisiert.";
            @__ctrl.Visible = false;
            ((System.Web.UI.IAttributeAccessor)(@__ctrl)).SetAttribute("style", "font-weight: 700; color: #009933");
            @__ctrl.ForeColor = ((System.Drawing.Color)(global::System.Drawing.Color.FromArgb(0, 153, 0)));
            return @__ctrl;
        }
        
        [global::System.ComponentModel.EditorBrowsableAttribute(global::System.ComponentModel.EditorBrowsableState.Never)]
        private global::System.Web.UI.WebControls.Label @__BuildControlRollenzuteilung_failed() {
            global::System.Web.UI.WebControls.Label @__ctrl;
            @__ctrl = new global::System.Web.UI.WebControls.Label();
            this.Rollenzuteilung_failed = @__ctrl;
            @__ctrl.ApplyStyleSheetSkin(this.Page);
            @__ctrl.ID = "Rollenzuteilung_failed";
            @__ctrl.Text = "Die personalisierte Rollenzuteilung wurde nicht vollständig abgeschlossen ...";
            @__ctrl.Visible = false;
            ((System.Web.UI.IAttributeAccessor)(@__ctrl)).SetAttribute("style", "font-weight: 700; color: #FF3300");
            @__ctrl.ForeColor = ((System.Drawing.Color)(global::System.Drawing.Color.FromArgb(102, 0, 102)));
            return @__ctrl;
        }
        
        [global::System.ComponentModel.EditorBrowsableAttribute(global::System.ComponentModel.EditorBrowsableState.Never)]
        private global::System.Web.UI.WebControls.Label @__BuildControlRollenzuteilung_failed_errormessage() {
            global::System.Web.UI.WebControls.Label @__ctrl;
            @__ctrl = new global::System.Web.UI.WebControls.Label();
            this.Rollenzuteilung_failed_errormessage = @__ctrl;
            @__ctrl.ApplyStyleSheetSkin(this.Page);
            @__ctrl.ID = "Rollenzuteilung_failed_errormessage";
            @__ctrl.Text = "";
            @__ctrl.Visible = false;
            ((System.Web.UI.IAttributeAccessor)(@__ctrl)).SetAttribute("style", "font-weight: 700; color: #FF3300");
            return @__ctrl;
        }
        
        [global::System.ComponentModel.EditorBrowsableAttribute(global::System.ComponentModel.EditorBrowsableState.Never)]
        private void @__BuildControlTree(global::Rollenzuteilung_WP.VisualWebPart1.VisualWebPart1 @__ctrl) {
            global::System.Web.UI.WebControls.Button @__ctrl1;
            @__ctrl1 = this.@__BuildControlButton1();
            System.Web.UI.IParserAccessor @__parser = ((System.Web.UI.IParserAccessor)(@__ctrl));
            @__parser.AddParsedSubObject(@__ctrl1);
            @__parser.AddParsedSubObject(new System.Web.UI.LiteralControl("\r\n</br>\r\n<div style=\"margin-left:15px;\">\r\n"));
            global::System.Web.UI.WebControls.Label @__ctrl2;
            @__ctrl2 = this.@__BuildControlRollenzuteilung_erledigt();
            @__parser.AddParsedSubObject(@__ctrl2);
            @__parser.AddParsedSubObject(new System.Web.UI.LiteralControl("\r\n"));
            global::System.Web.UI.WebControls.Label @__ctrl3;
            @__ctrl3 = this.@__BuildControlRollenzuteilung_failed();
            @__parser.AddParsedSubObject(@__ctrl3);
            @__parser.AddParsedSubObject(new System.Web.UI.LiteralControl("\r\n"));
            global::System.Web.UI.WebControls.Label @__ctrl4;
            @__ctrl4 = this.@__BuildControlRollenzuteilung_failed_errormessage();
            @__parser.AddParsedSubObject(@__ctrl4);
            @__parser.AddParsedSubObject(new System.Web.UI.LiteralControl("\r\n</div>\r\n"));
        }
        
        private void InitializeControl() {
            this.@__BuildControlTree(this);
            this.Load += new global::System.EventHandler(this.Page_Load);
        }
        
        [global::System.ComponentModel.EditorBrowsableAttribute(global::System.ComponentModel.EditorBrowsableState.Never)]
        protected virtual object Eval(string expression) {
            return global::System.Web.UI.DataBinder.Eval(this.Page.GetDataItem(), expression);
        }
        
        [global::System.ComponentModel.EditorBrowsableAttribute(global::System.ComponentModel.EditorBrowsableState.Never)]
        protected virtual string Eval(string expression, string format) {
            return global::System.Web.UI.DataBinder.Eval(this.Page.GetDataItem(), expression, format);
        }
    }
}
