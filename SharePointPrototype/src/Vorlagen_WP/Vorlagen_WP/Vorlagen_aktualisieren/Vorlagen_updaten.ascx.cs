using System;
using System.ComponentModel;
using System.Collections.Generic;
using System.Web.UI.WebControls.WebParts;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;
using Microsoft.SharePoint.Workflow;
using Microsoft.SharePoint.Utilities;


namespace Vorlagen_WP.VisualWebPart1
{
    [ToolboxItemAttribute(false)]
    public partial class Vorlagen_updaten : WebPart
    {
        // Uncomment the following SecurityPermission attribute only when doing Performance Profiling using
        // the Instrumentation method, and then remove the SecurityPermission attribute when the code is ready
        // for production. Because the SecurityPermission attribute bypasses the security check for callers of
        // your constructor, it's not recommended for production purposes.
        // [System.Security.Permissions.SecurityPermission(System.Security.Permissions.SecurityAction.Assert, UnmanagedCode = true)]
        public Vorlagen_updaten()
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
 

 
        protected void Button1_Click(object sender, EventArgs e)
        {
            bool success = true;

            SPWeb contextWeb = SPContext.Current.Web;
            SPList projektdaten = contextWeb.Lists["Projektdaten"];
            String Projektname = "";
            String Firma = "";
            String Abteilung = "";
            String Auftraggeber = "";
            String Projektleiter = "";

            foreach (SPListItem item in projektdaten.Items)
            {

                try
                {
                    Projektname = item["Projektname"].ToString();
                    Firma = item["Company"].ToString(); // native name
                    Abteilung = item["Abteilung"].ToString();
                    Auftraggeber = item["Auftraggeber"].ToString();
                    Projektleiter = item["Projektleiter"].ToString();
                    
                    SuccessMessage.Text = "Projektdaten eingelesen";
                    SuccessMessage.Visible = true;
                }
                catch (Exception ex)
                {
                    success = false;
                    ErrorMessage.Text = ex.Message;
                    ErrorMessage.Visible = true;


                }
            }




            SPList dokumentenvorlagen = contextWeb.Lists["Dokumentenvorlagen"];

            foreach (SPListItem item in dokumentenvorlagen.Items)
            {
                try
                {
                    item["Projektname"] = Projektname;

                    // Special Case to reflect changes from Firma to AMT, remain yet backwards compatible - 18 April 2014 GZA
                    try
                    {
                        item["Amt"] = Firma;
                    }
                    catch
                    {
                        item["Firma"] = Firma;
                    }
                    
                    item["Abteilung"] = Abteilung;
                    item["Auftraggeber"] = Auftraggeber;
                    item["Projektleiter"] = Projektleiter;
                    /* */
                    item.Update();

                }
                catch (Exception ex)
                {
                    success = false;
                    ErrorMessage.Text = ex.Message;
                    ErrorMessage.Visible = true;

                }

            }

            // loop also the default focs
            SPList projektablage = contextWeb.Lists["Projektablage"];

            //List all the HERMES5 default documents
            // important lower case, staying compatible with java versions
            IList<string> hermesDocNameList = (IList<string>)new[] { "abnahmeprotokoll.docx",
                "aenderungsantrag.docx",
                "aenderungsstatusliste.docx",
                "arbeitsauftrag.docx",
                "betriebshandbuch.docx",
                "betriebskonzept.docx",
                "checklisten.docx",
                "detialstudie.docx",
                "einfuehrungskonzept.docx",
                "evaluationsbericht.docx",
                "geschaeftsorganisationskonzept.docx",
                "integrationskonzept.docx",
                "liste_der_stakeholder.docx",
                "migrationskonzept.docx",
                "phasenbericht.docx",
                "projektauftrag.docx",
                "projektentscheide.docx",
                "prototypdokumentation.docx",
                "pruefprotokoll.xlsx",
                "kriterienkatalog.xlsx",
                "lastenheft.docx",
                "pendenzenliste.xlsx",
                "detailstudie.docx",
                "kriterienkatalog.docx",
                "lastenheft.docx",
                "projekterfahrungen.docx",
                "projektinitialisierungsauftrag.docx",
                "projektmanagementplan.docx",
                "projektschlussbeurteilung.docx",
                "projektstatusbericht.docx",
                "protokoll.docx",
                "rechtsgrundlagenanalyse.docx",
                "situationsanalyse.docx",
                "studie.docx",
                "systemanforderungen.docx",
                "systemarchitektur.docx",
                "testkonzept.docx",
                "testprotokoll.docx"
                 };

            bool b;
            foreach (SPListItem item in projektablage.Items)
            {
                string hermesDocname = item.Name.ToLower();
                b = hermesDocNameList.Contains(hermesDocname);
                if (b==true){

                    try
                    {
                        item["Projektname"] = Projektname;

                        // Special Case to reflect changes from Firma to AMT, remain yet backwards compatible - 18 April 2014 GZA
                        try
                        {
                            item["Amt"] = Firma;
                        }
                        catch
                        {
                            item["Firma"] = Firma;
                        }

                        item["Abteilung"] = Abteilung;
                        item["Auftraggeber"] = Auftraggeber;
                        item["Projektleiter"] = Projektleiter;
                        /* */
                        item.Update();

                    }
                    catch (Exception ex)
                    {
                        success = false;
                        ErrorMessage.Text = ex.Message +" bei " + hermesDocname;
                        ErrorMessage.Visible = true;

                    }
                }
            }


            // Fill default values
            try
            {

                SPField f_projektname = dokumentenvorlagen.Fields["Projektname"];
                f_projektname.DefaultValue = Projektname;
                f_projektname.Update();

                // Special Case to reflect changes from Firma to AMT, remain yet backwards compatible - 18 April 2014 GZA
                try
                {
                    SPField f_firma = dokumentenvorlagen.Fields["Amt"];
                    f_firma.DefaultValue = Firma;
                    f_firma.Update();
                }
                catch
                {
                    SPField f_firma = dokumentenvorlagen.Fields["Firma"];
                    f_firma.DefaultValue = Firma;
                    f_firma.Update();
                }



                SPField f_abteilung = dokumentenvorlagen.Fields["Abteilung"];
                f_abteilung.DefaultValue = Abteilung;
                f_abteilung.Update();

                SPField f_auftraggeb = dokumentenvorlagen.Fields["Auftraggeber"];
                f_auftraggeb.DefaultValue = Auftraggeber;
                f_auftraggeb.Update();

                SPField f_pm = dokumentenvorlagen.Fields["Projektleiter"];
                f_pm.DefaultValue = Projektleiter;
                f_pm.Update();
            }

            catch (Exception ex)
            {
                success = false;
                ErrorMessage.Text = ex.Message;
                ErrorMessage.Visible = true;

            }

            if (success)
            {

                SuccessMessage.Text = "Projektdaten wurden komplett in die Vorlagen übertragen";
                SuccessMessage.Visible = true;
            }



        }
    }
}
