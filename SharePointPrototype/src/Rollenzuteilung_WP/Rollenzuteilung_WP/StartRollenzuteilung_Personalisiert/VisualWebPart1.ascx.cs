using System;
using System.ComponentModel;
using System.Web.UI.WebControls.WebParts;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;
using Microsoft.SharePoint.Workflow;
using Microsoft.SharePoint.Utilities;

namespace Rollenzuteilung_WP.VisualWebPart1
{
    [ToolboxItemAttribute(false)]
    public partial class VisualWebPart1 : WebPart
    {
        // Uncomment the following SecurityPermission attribute only when doing Performance Profiling using
        // the Instrumentation method, and then remove the SecurityPermission attribute when the code is ready
        // for production. Because the SecurityPermission attribute bypasses the security check for callers of
        // your constructor, it's not recommended for production purposes.
        // [System.Security.Permissions.SecurityPermission(System.Security.Permissions.SecurityAction.Assert, UnmanagedCode = true)]
        public VisualWebPart1()
        {
        }

        protected override void OnInit(EventArgs e)
        {
            base.OnInit(e);
            InitializeControl();
        }

        protected void Page_Load(object sender, EventArgs e)
        {
        }
        /// <summary>
        /// Assigns the role user to the AUFGABE (Task)
        /// </summary>
        protected void Button1_Click(object sender, EventArgs e)
        {
            bool success = true;

            SPWeb contextWeb = SPContext.Current.Web;
            SPList rollenzuteilung = contextWeb.Lists["Rollenzuteilung"];

            foreach (SPListItem item in rollenzuteilung.Items)

            {
                try
                {

                    String rolle = item["Rolle"].ToString();
                    if (rolle != null)
                    {

                        if (item["Verantwortlich"] != null)
                        {
                            SPFieldUser assignedTo = (SPFieldUser)rollenzuteilung.Fields["Verantwortlich"];
                            SPFieldUserValue user = (SPFieldUserValue)assignedTo.GetFieldValue(item["Verantwortlich"].ToString());
                            SPUser userObject = user.User;
                            UpdateAufgabe(rolle, userObject);
                            UpdateErgebnisse(rolle, userObject);
                        }
                    }

                }
                catch (Exception ex)
                {
                    success = false;
                    if (Rollenzuteilung_failed_errormessage.Text != ex.Message)
                    {
                        Rollenzuteilung_failed_errormessage.Text = Rollenzuteilung_failed_errormessage.Text + " " + ex.Message;
                    }

                }

            }
            if (success)
            {
                Rollenzuteilung_erledigt.Visible = true;


            }
            else
            {
                Rollenzuteilung_failed.Visible = true;
                Rollenzuteilung_failed_errormessage.Visible = true;
            }

        }



        private void UpdateAufgabe(String rolle, SPUser assignedto)
        {
            SPWeb contextWeb = SPContext.Current.Web;
            SPList aufgaben = contextWeb.Lists["Aufgaben"];
            String aufgabenrolle;

            foreach (SPListItem item in aufgaben.Items)
            {

                aufgabenrolle = item["Rolle"].ToString();

                if(rolle == item["Rolle"].ToString())
                {
                    
                    if(item["Zugewiesen an"]== null) {
                        item["Zugewiesen an"] = assignedto;
                        item.Update();
                    }
                }
            }

        }

        private void UpdateErgebnisse(String rolle, SPUser assignedto)
        {
            SPWeb contextWeb = SPContext.Current.Web;
            SPList aufgaben = contextWeb.Lists["Ergebnisse"];
            String aufgabenrolle;

            foreach (SPListItem item in aufgaben.Items)
            {

                aufgabenrolle = item["Rolle"].ToString();

                if (rolle == item["Rolle"].ToString())
                {

                    if (item["Zugewiesen an"] == null)
                    {
                        item["Zugewiesen an"] = assignedto;
                        item.Update();
                    }



                }
            }

        }
    }
}
