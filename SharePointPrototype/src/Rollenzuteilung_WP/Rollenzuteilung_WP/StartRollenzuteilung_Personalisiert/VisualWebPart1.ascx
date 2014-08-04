<%@ Assembly Name="$SharePoint.Project.AssemblyFullName$" %>
<%@ Assembly Name="Microsoft.Web.CommandUI, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="asp" Namespace="System.Web.UI" Assembly="System.Web.Extensions, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35" %>
<%@ Import Namespace="Microsoft.SharePoint" %> 
<%@ Register Tagprefix="WebPartPages" Namespace="Microsoft.SharePoint.WebPartPages" Assembly="Microsoft.SharePoint, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Control Language="C#" AutoEventWireup="true" CodeBehind="VisualWebPart1.ascx.cs" Inherits="Rollenzuteilung_WP.VisualWebPart1.VisualWebPart1" %>
<asp:Button ID="Button1" runat="server" OnClick="Button1_Click" Text="Der für die Rolle verantwortlichen Person die Aufgaben und Ergebnisse jetzt zuweisen ...." ToolTip="Nachdem Sie den Rollen die verantwortliche Person zugewiesen haben, können Sie den Aufgaben und Ergebnissen, die noch keine personalisierte Zuteilung aufweisen, die entsprechende verantwortliche Person zuordnen." />
</br>
<div style="margin-left:15px;">
<asp:Label ID="Rollenzuteilung_erledigt" runat="server" Text="Die personalisierte Rollenzuteilung wurde aktualisiert." Visible="False" style="font-weight: 700; color: #009933" ForeColor="#009900"></asp:Label>
<asp:Label ID="Rollenzuteilung_failed" runat="server" Text="Die personalisierte Rollenzuteilung wurde nicht vollständig abgeschlossen ..." Visible="False" style="font-weight: 700; color: #FF3300" ForeColor="#660066"></asp:Label>
<asp:Label ID="Rollenzuteilung_failed_errormessage" runat="server" Text="" Visible="False" style="font-weight: 700; color: #FF3300"></asp:Label>
</div>
