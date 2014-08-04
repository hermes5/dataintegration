<%@ Assembly Name="$SharePoint.Project.AssemblyFullName$" %>
<%@ Assembly Name="Microsoft.Web.CommandUI, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint" Namespace="Microsoft.SharePoint.WebControls" Assembly="Microsoft.SharePoint, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities" Namespace="Microsoft.SharePoint.Utilities" Assembly="Microsoft.SharePoint, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="asp" Namespace="System.Web.UI" Assembly="System.Web.Extensions, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35" %>
<%@ Import Namespace="Microsoft.SharePoint" %> 
<%@ Register Tagprefix="WebPartPages" Namespace="Microsoft.SharePoint.WebPartPages" Assembly="Microsoft.SharePoint, Version=15.0.0.0, Culture=neutral, PublicKeyToken=71e9bce111e9429c" %>
<%@ Control Language="C#" AutoEventWireup="true" CodeBehind="Vorlagen_updaten.ascx.cs" Inherits="Vorlagen_WP.VisualWebPart1.Vorlagen_updaten" %>
<asp:Button ID="Button1" runat="server" OnClick="Button1_Click" Text="Vorlagen aktualisieren" ToolTip="Bei Änderungen der Projektdaten können Sie hier die Dokumentenvorlagen aktualisieren" />
<p style="margin-left:15px;">
    <asp:Label ID="SuccessMessage" runat="server" style="color: #009900" Text="Erfolgsmeldung" Visible="False"></asp:Label>
    <br />
    <asp:Label ID="ErrorMessage" runat="server" style="color: #CC3300" Text="Fehlermeldung" Visible="False"></asp:Label>
</p>

