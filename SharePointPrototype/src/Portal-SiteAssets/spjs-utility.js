/*********************************
 * Created by Alexander Bautz
 * alexander.bautz@gmail.com
 * http://sharepointjavascript.wordpress.com
 * Copyright (c) 2009-2013 Alexander Bautz (Licensed under the MIT X11 License)
 **********************************/
if (typeof (spjs) === 'undefined') {
    var spjs = {};
}

spjs.utility = {
    version: 1.173,
    versionDate: "September 13, 2013"
};

// Compatibility with older solutions
if (typeof (_spjs) === 'undefined') {
    var _spjs = {};
}
_spjs.utility = {
    version: 1.173,
    versionDate: "September 13, 2013"
};
// Compatibility with older solutions

function doResizeDialog() {
    if (GetUrlKeyValue('IsDlg') !== '1') {
        return;
    }
    var pWh, wpd, dlgBorder, ribbonH, dlgOuterFrame, dlgInnerFrame, dlgContent, formH, top, cssObj, dTop;
    pWh = $(window.parent.document).height() - 172;
    wpd = $(window.parent.document);
    dlgBorder = wpd.find("div.ms-dlgBorder");
    ribbonH = $("#s4-ribbonrow").height() + 103;
    dlgInnerFrame = wpd.find("iframe[id^='DlgFrame']");
    dlgContent = dlgInnerFrame.parent().parent().parent();
    dlgOuterFrame = dlgContent.prev();
    if (dlgContent.attr('DragBehavior') !== null && dlgContent.css('left') !== '10px') {
        formH = $("#onetIDListForm").height() + ribbonH;
        if (formH > pWh) {
            formH = pWh;
        }
        if (pWh !== formH) {
            top = (pWh - formH) / 2;
            if (top < 0) {
                top = 10;
            }
        } else {
            top = 10;
        }
        dlgBorder.css({
            "height": formH + 'px'
        });
        cssObj = {
            position: 'absolute',
            height: formH + 'px'
        };
        dTop = parseInt(dlgOuterFrame.css('top'), 10);
        if (dTop + formH > pWh) {
            cssObj.top = top + "px"
        }
        dlgOuterFrame.css(cssObj);
        dlgContent.css(cssObj);
        dlgInnerFrame.css({
            height: (formH - 32) + 'px'
        });
    }
}

function spjs_AddList(listName, listBaseUrl, listDescription) {
    var c, r;
    c = [];
    c.push("<AddList xmlns='http://schemas.microsoft.com/sharepoint/soap/'>");
    c.push("<listName>" + listName + "</listName>");
    c.push("<description>" + listDescription + "</description>");
    c.push("<templateID>100</templateID>");
    c.push("</AddList>");
    r = {
        success: false
    };
    spjs_wrapSoapRequest(listBaseUrl + "/_vti_bin/lists.asmx", "http://schemas.microsoft.com/sharepoint/soap/AddList", c.join(''), function (data) {
        if ($(data).find('ErrorText').length > 0) {
            r.errorText = $(data).find('ErrorText').text();
            r.errorCode = $(data).find('ErrorCode').text();
        } else {
            r.success = true;
            r.id = $(data).find('List').attr('ID');
        }
    });
    return r;
}

function spjs_UpdateList(listName, listBaseUrl, newFieldsObjArr, updFieldsObjArr) {
    var nb, ub, mi, c, cb, fb, r, addToView;
    nb = [];
    ub = [];
    mi = 1;
    $.each(newFieldsObjArr, function (i, obj) {
        c = '';
        fb = [];
        addToView = false;
        $.each(obj, function (p, v) {
            if (p === 'content') {
                c = v;
            } else {
                if (p === 'AddToView') {
                    addToView = true;
                } else {
                    fb.push(" " + p + "=\"" + v + "\"");
                }
            }
        });
        nb.push("<Method ID='" + mi + "'");
        if (addToView) {
            nb.push(" AddToView=''");
        }
        nb.push("><Field " + fb.join(''));
        if (c === '') {
            nb.push(" /></Method>");
        } else {
            nb.push(">" + c + "</Field></Method>");
        }
        mi++;
    });
    $.each(updFieldsObjArr, function (i, obj) {
        c = '';
        fb = [];
        addToView = false;
        $.each(obj, function (p, v) {
            if (p === 'content') {
                c = v;
            } else {
                if (p === 'AddToView') {
                    addToView = true;
                } else {
                    fb.push(" " + p + "=\"" + v + "\"");
                }
            }
        });
        ub.push("<Method ID='" + (mi) + "'");
        if (addToView) {
            ub.push(" AddToView=''");
        }

        ub.push("><Field " + fb.join(''));
        if (c === '') {
            ub.push(" /></Method>");
        } else {
            ub.push(">" + c + "</Field></Method>");
        }
        mi++;
    });
    cb = [];
    cb.push("<UpdateList xmlns='http://schemas.microsoft.com/sharepoint/soap/'>");
    cb.push("<listName>" + listName + "</listName>");
    if (nb.length > 0) {
        cb.push("<newFields>");
        cb.push("<Fields>");
        cb.push(nb.join(''));
        cb.push("</Fields>");
        cb.push("</newFields>");
    }
    if (ub.length > 0) {
        cb.push("<updateFields>");
        cb.push("<Fields>");
        cb.push(ub.join(''));
        cb.push("</Fields>");
        cb.push("</updateFields>");
    }
    cb.push("</UpdateList>");
    r = {
        success: false
    };
    spjs_wrapSoapRequest(listBaseUrl + "/_vti_bin/lists.asmx", "http://schemas.microsoft.com/sharepoint/soap/UpdateList", cb.join(''), function (data) {
        if ($(data).find('ErrorText').length > 0) {
            r.errorText = $(data).find('ErrorText').text();
            r.errorCode = $(data).find('ErrorCode').text();
        } else {
            r.success = true;
        }
    });
    return r;
}

function getUserInfo_v2(loginOrUserID) {
    var arrOfFields, query, res, result, item, webUrl;
    result = {
        success: false
    };
    arrOfFields = ['ID', 'Name', 'Title', 'EMail', 'Department', 'JobTitle', 'Notes', 'Picture', 'IsSiteAdmin', 'Created', 'Author', 'Modified', 'Editor', 'SipAddress'];
    if (parseInt(loginOrUserID, 10).toString() === loginOrUserID.toString()) {
        query = "<Where><Eq><FieldRef Name='ID' /><Value Type='Integer'>" + loginOrUserID + "</Value></Eq></Where>";
    } else {
        query = "<Where><Eq><FieldRef Name='Name' /><Value Type='Text'>" + loginOrUserID + "</Value></Eq></Where>";
    }
    res = spjs_QueryItems({
        'listName': 'UserInfo',
        'listBaseUrl': L_Menu_BaseUrl,
        'query': query,
        'viewFields': arrOfFields,
        setRequestHeader: false
    });
    if (res.count > 0) {
        result.success = true;
        item = res.items[0];
        $.each(arrOfFields, function (i, fin) {
            result[arrOfFields[i]] = item[arrOfFields[i]];
        });
        return result;
    } else {
        result.success = false;
        return result;
    }
}

function spjs_getItemByID(argObj) {
    var query, qRes;
    query = "<Where><Eq><FieldRef Name='ID' /><Value Type='Text'>" + argObj.id + "</Value></Eq></Where>";
    qRes = spjs_QueryItems({
        'listName': argObj.listName,
        'listBaseUrl': argObj.listBaseUrl,
        'query': query,
        'viewFields': argObj.viewFields
    });
    if (qRes.count === 0) {
        return null;
    } else {
        return qRes.items[0];
    }
}

// http://www.steveworkman.com/html5-2/javascript/2011/improving-javascript-xml-node-finding-performance-by-2000/
$.fn.filterNode = function (name) {
    return this.find('*').filter(function () {
        return this.nodeName === name;
    });
};

function spjs_QueryItems(argObj) {
    var content, result, requestHeader, currID, fieldValObj, value;
    if (argObj.listBaseUrl === undefined) {
        argObj.listBaseUrl = L_Menu_BaseUrl;
    }
    if (argObj.listName === undefined || (argObj.query === undefined && argObj.viewName === undefined)) {
        alert("[spjs_QueryItems]\n\nMissing parameters!\n\nYou must provide a minimum of \"listName\", \"query\" or \"viewName\" and \"viewFields\".");
        return;
    }
    if ($.inArray('ID', argObj.viewFields) === -1) {
        argObj.viewFields.push('ID');
    }

    content = spjs_wrapQueryContent({
        'listName': argObj.listName,
        'query': argObj.query,
        'viewName': argObj.viewName,
        'viewFields': argObj.viewFields,
        'rowLimit': argObj.rowLimit,
        'pagingInfo': argObj.pagingInfo
    });
    result = {
        'count': -1,
        'nextPagingInfo': '',
        items: []
    };
    if (argObj.setRequestHeader === false) {
        requestHeader = '';
    } else {
        requestHeader = 'http://schemas.microsoft.com/sharepoint/soap/GetListItems'
    }
    spjs_wrapSoapRequest(argObj.listBaseUrl + '/_vti_bin/lists.asmx', requestHeader, content, function (data) {
        result.count = parseInt($(data).filterNode("rs:data").attr('ItemCount'), 10);
        result.nextPagingInfo = $(data).filterNode("rs:data").attr('ListItemCollectionPositionNext');
        fieldValObj = {}
        $(data).filterNode('z:row').each(function (idx, itemData) {
            currID = $(itemData).attr('ows_ID');
            fieldValObj[currID] = {};
            $.each(argObj.viewFields, function (i, field) {
                value = $(itemData).attr('ows_' + field);
                if (value === undefined) {
                    value = null;
                }
                fieldValObj[currID][field] = value;
            });
            result.items.push(fieldValObj[currID]);
        });
    });
    return result;
}

function spjs_wrapQueryContent(paramObj) {
    var result = [];
    result.push('<GetListItems xmlns="http://schemas.microsoft.com/sharepoint/soap/">');
    result.push('<listName>' + paramObj.listName + '</listName>');
    if (paramObj.viewName !== undefined && paramObj.viewName !== '') {
        result.push('<viewName>' + paramObj.viewName + '</viewName>');
    }
    if (paramObj.query !== null && paramObj.query !== '') {
        result.push('<query><Query xmlns="">');
        result.push(paramObj.query);
        result.push('</Query></query>');
    }
    if (paramObj.viewFields !== undefined && paramObj.viewFields !== '' && paramObj.viewFields.length > 0) {
        result.push('<viewFields><ViewFields xmlns="">');
        $.each(paramObj.viewFields, function (idx, field) {
            result.push('<FieldRef Name="' + field + '"/>');
        });
        result.push('</ViewFields></viewFields>');
    }
    // A view overrides the itemlimit
    if (paramObj.viewName === undefined) {
        if (paramObj.rowLimit !== undefined && paramObj.rowLimit > 0) {
            result.push('<rowLimit>' + paramObj.rowLimit + '</rowLimit>');
        } else {
            result.push('<rowLimit>100000</rowLimit>');
        }
    }
    result.push('<queryOptions><QueryOptions xmlns=""><IncludeMandatoryColumns>FALSE</IncludeMandatoryColumns>');
    if (paramObj.pagingInfo !== undefined && paramObj.pagingInfo !== null && paramObj.pagingInfo !== '') {
        result.push('<Paging ListItemCollectionPositionNext="' + paramObj.pagingInfo.replace(/&/g, '&amp;') + '" />');
    }
    result.push('</QueryOptions></queryOptions>');
    result.push('</GetListItems>');
    return result.join('');
}

function spjs_wrapSoapRequest(webserviceUrl, requestHeader, soapBody, successFunc) {
    var xmlWrap = [];
    xmlWrap.push("<?xml version='1.0' encoding='utf-8'?>");
    xmlWrap.push("<soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>");
    xmlWrap.push("<soap:Body>");
    xmlWrap.push(soapBody);
    xmlWrap.push("</soap:Body>");
    xmlWrap.push("</soap:Envelope>");
    xmlWrap = xmlWrap.join('');
    $.ajax({
        async: false,
        type: "POST",
        url: webserviceUrl,
        contentType: "text/xml; charset=utf-8",
        processData: false,
        data: xmlWrap,
        dataType: "xml",
        beforeSend: function (xhr) {
            if (requestHeader !== '') {
                xhr.setRequestHeader('SOAPAction', requestHeader);
            }
        },
        success: successFunc,
        error: function (xhr, ajaxOptions, thrownError) {
            //alert(xhr.status);
            //alert(thrownError);
        }
    });
}

function setFieldValue(fin, newVal, onLoad) {
    if (typeof (onLoad) === 'undefined') {
        onLoad = true;
    }
    // If not already done - init all fields
    if (typeof (fields) === 'undefined') {
        fields = init_fields_v2();
    }
    // Return if FieldInternalName is not found
    if (fields[fin] === undefined) {
        return;
    }
    var isSP13, thisField, fieldType, optText, inputField, inputFieldHidden, split, newValueBuffer, id, friendlyVal;
    isSP13 = false;
    if (typeof _spPageContextInfo !== "undefined" && _spPageContextInfo.webUIVersion === 15) {
        isSP13 = true;
    }
    thisField = $(fields[fin]);
    fieldType = $(fields[fin]).attr('FieldType');
    if (fieldType === undefined) {
        alert("[setFieldValue]\nThe attribute \"FieldType\" is missing for the FieldInternalName \"" + fin + "\".");
        return false;
    }
    switch (fieldType) {
        case 'ContentTypeChoice':
            thisField.find('select option').each(function () {
                if ($(this).text() == newVal) {
                    $(this).attr('selected', 'selected').parent().change();
                }
            });
            break;
        case 'SPFieldText':
        case 'SPFieldFile':
        case 'SPFieldNumber':
        case 'SPFieldCurrency':
            thisField.find('input').val(newVal);
            break;
        case 'SPFieldChoice':
            if (thisField.find('input:radio').length > 0) {
                thisField.find('input:radio').each(function () {
                    if ($(this).next().text() === newVal) {
                        $(this).attr('checked', 'checked');
                    }
                });
            } else {
                thisField.find('select option').each(function () {
                    if ($(this).text() == newVal) {
                        $(this).prop('selected', true);
                    }
                });
            }
            break;
        case 'SPFieldMultiChoice':
            if (typeof (newVal) !== 'object') {
                newVal = newVal.split(',');
            }
            thisField.find('input:checkbox').each(function () {
                optText = $(this).next().text();
                if ($.inArray(optText, newVal) > -1) {
                    $(this).prop('checked', true);
                } else {
                    $(this).prop('checked', false);
                }
            });
            break;
        case 'SPFieldUser':
        case 'SPFieldUserMulti':
            if (isSP13) {
                ExecuteOrDelayUntilScriptLoaded(function () {
                    var p = SPClientPeoplePicker.SPClientPeoplePickerDict[thisField.find("div.sp-peoplepicker-topLevel").attr("id")];
                    if (!p.AllowMultipleUsers) {
                        p.DeleteProcessedUser();
                    }
                    if (!$.isArray(newVal)) {
                        newVal = newVal.split(/;|,/);
                    }
                    $.each(newVal, function (i, v) {
                        document.getElementById(p.EditorElementId).value = $.trim(v);
                        p.AddUnresolvedUserFromEditor(true);
                    });
                }, 'sp.ribbon.js');
            } else {
                $(document).ready(function () {
                    if (browseris.ie5up && browseris.win32 && !IsAccessibilityFeatureEnabled()) {
                        thisField.find('div.ms-inputuserfield').html(newVal);
                        thisField.find('img:first').click();
                    } else {
                        thisField.find('textarea:first').val(newVal);
                    }
                });
            }
            break;
        case 'SPFieldLookup':
            thisField.find('select option').each(function () {
                if ($(this).text() === newVal) { // By text value				
                    $(this).parent().val($(this).val());
                    return false;
                }
                if ($(this).val() === newVal) { // By ID		
                    $(this).parent().val($(this).val());
                    return false;
                }
            });
            break;
        case 'SPFieldLookup_Input':
            inputField = thisField.find('input.ms-lookuptypeintextbox');
            inputFieldHidden = $("*[id='" + inputField.attr('optHid') + "']");
            split = inputField.attr('choices').split('|');
            var match = false;
            // By text
            $.each(split, function (i, val) {
                if (i % 2 !== 0) {
                    return;
                }
                if (val === newVal) {
                    inputField.val(split[i]);
                    inputFieldHidden.val(split[i + 1]);
                    match = true;
                    return false;
                }
            });
            // By ID
            if (!match) {
                $.each(split, function (i, val) {
                    if (i % 2 === 0) {
                        return;
                    }
                    if (val === newVal) {
                        inputField.val(split[i - 1]);
                        inputFieldHidden.val(split[i]);
                        return false;
                    }
                });
            }
            break;
        case 'SPFieldLookupMulti':
            if (onLoad) {
                if (typeof (newVal) !== 'object') {
                    newVal = newVal.split(',');
                }
                split = thisField.find('input:hidden[id$="Picker_data"]').val().split('|t');
                newValueBuffer = [];
                $.each(split, function (i, val) {
                    if (i % 4 != 0) {
                        return;
                    }
                    id = val;
                    friendlyVal = split[i + 1];
                    if ($.inArray(friendlyVal, newVal) > -1) {
                        newValueBuffer.push(id + '|t' + friendlyVal);
                    }
                });
                thisField.find('input:hidden[id$="Picker_initial"]').val(newValueBuffer.join('|t'));
            } else {
                alert("Setting the value for a field of type " + fieldType + " is supported only \"onLoad\".");
            }
            break;
        case 'SPFieldBoolean':
            if (newVal == 1 || newVal == '1' || newVal == true) {
                if (parseInt($.fn.jquery.split(".")[1]) >= 6) {
                    thisField.find('input').prop('checked', 'checked');
                } else {
                    thisField.find('input').attr('checked', 'checked');
                }
            } else {
                thisField.find('input').removeAttr('checked');
            }
            break;
        case 'SPFieldURL':
            if (typeof (newVal) !== 'object') {
                newVal = newVal.split(',');
            }
            thisField.find('input:first').val(newVal[0]);
            thisField.find('input:last').val(newVal[1]);
            break;
        case 'SPFieldDateTime':
            if (typeof (newVal) !== 'object') {
                newVal = newVal.split(',');
            }
            thisField.find('input:first').val(newVal[0]);
            if (newVal[1] !== undefined) {
                thisField.find('select:first option:eq(' + newVal[1] + ')').prop("selected", "selected");
            }
            if (newVal[2] !== undefined) {
                newVal[2] = newVal[2] - (newVal[2] % 5);
                thisField.find('select:last').val(newVal[2]);
            }
            break;
        case 'SPFieldNote':
            if (thisField.find('textarea').length === 0) {
                thisField.find("div[contenteditable='true']").html(newVal);
            } else {
                thisField.find('textarea:first').val(newVal);
            }
            break;
        case 'SPFieldNote_HTML':
            if (onLoad) {
                thisField.find('textarea:first').val(newVal);
            } else {
                if (browseris.ie5up && browseris.win32 && !IsAccessibilityFeatureEnabled()) {
                    thisField.find('iframe.ms-rtelong').contents().find('body').html('<div>' + newVal + '</div>');
                } else {
                    thisField.find('textarea:first').val(newVal);
                }
            }
            break;
        case 'SPFieldNote_EHTML':
            thisField.find("div[id$='TextField_inplacerte']").html(newVal);
            break;
        case 'SPFieldTaxonomyFieldType':
        case 'SPFieldTaxonomyFieldTypeMulti':
            var t = new Microsoft.SharePoint.Taxonomy.ControlObject(thisField.find("div.ms-taxonomy")[0]);
            t.setTextAndCursor(newVal, false);
            t.validateAll();
            break;
        default:
            alert("Unknown fieldType: " + fieldType);
    }
}

function getFieldValue(fin, dispform, multiValueJoinBy, optionalFilter) {
    // If not already done - init all fields
    if (typeof (fields) === 'undefined') {
        fields = init_fields_v2();
    }
    // Return if FieldInternalName is not found
    if (fields[fin] === undefined) {
        return;
    }
    var isSP13, thisField, valRaw, fieldType, getFieldValueReturnVal, multiChoice, userMulti, lookupMulti, attrToFind, thisVal, link, descr, date, hour, minutes, AMPM, hourRaw, minutesRaw;
    thisField = $(fields[fin]);
    if (dispform == true) {
        valRaw = $(fields[fin]).find('.ms-formbody').text();
        return $.trim(valRaw.replace(/[ \xA0]+$/, '')) == '' ? '' : $.trim(valRaw.replace(/[ \xA0]+$/, ''));
    } else { // If "editform"
        isSP13 = false;
        if (typeof _spPageContextInfo !== "undefined" && _spPageContextInfo.webUIVersion === 15) {
            isSP13 = true;
        }
        fieldType = $(fields[fin]).attr('FieldType');
        if (fieldType === undefined) {
            alert("[getFieldValue]\nThe attribute \"FieldType\" is missing for the FieldInternalName \"" + fin + "\".\nEnsure that you use the function init_fields_v2().");
            return false;
        }
        getFieldValueReturnVal = '';
        switch (fieldType) {
            case 'ContentTypeChoice':
                getFieldValueReturnVal = thisField.find('option:selected').text();
                break;
            case 'Attachments':
                multiChoice = [];
                thisField.find('#idAttachmentsTable tr').each(function (i, tr) {
                    multiChoice.push($(tr).find('td:first').html());
                });
                getFieldValueReturnVal = multiChoice.join("<br>");
                break;
            case 'SPFieldText':
            case 'SPFieldFile':
            case 'SPFieldNumber':
            case 'SPFieldCurrency':
                getFieldValueReturnVal = thisField.find('input').val();
                break;
            case 'SPFieldChoice':
                if (thisField.find('input:radio').length > 0 && thisField.find('select').length === 0 && thisField.find('input:text').length === 0) {
                    getFieldValueReturnVal = thisField.find('input:radio:checked').next().text();
                } else if (thisField.find('input:radio').length > 0 && thisField.find('input:text').length > 0) {
                    // Fill-in
                    if (thisField.find('select').length > 0) {
                        if (thisField.find('input:radio:last').prop('checked')) {
                            getFieldValueReturnVal = $.trim(thisField.find('input:text').val());
                        } else {
                            getFieldValueReturnVal = thisField.find('select').val();
                        }
                    } else {
                        if (thisField.find('input:radio:last').prop('checked')) {
                            getFieldValueReturnVal = $.trim(thisField.find('input:text').val());
                        } else {
                            getFieldValueReturnVal = thisField.find('input:radio:checked').next().text();
                        }
                    }
                } else {
                    getFieldValueReturnVal = thisField.find('select').val();
                }
                break;
            case 'SPFieldMultiChoice':
                multiChoice = [];
                thisField.find('input:checkbox').each(function (i, opt) {
                    if ($(opt).prop("checked")) {
                        opt = $(opt);
                        if (thisField.find('input:text').length === 0) { // No fill-in
                            multiChoice.push(opt.next().text());
                        } else { // Fill-in
                            if (i + 1 === thisField.find('input:checkbox').length) {
                                multiChoice.push(thisField.find('input:text').val());
                            } else {
                                multiChoice.push(opt.next().text());
                            }
                        }
                    }
                });
                if (multiValueJoinBy !== undefined && multiValueJoinBy !== '') {
                    getFieldValueReturnVal = multiChoice.join(multiValueJoinBy);
                } else {
                    getFieldValueReturnVal = multiChoice;
                }
                break;
            case 'SPFieldUser':
            case 'SPFieldUserMulti':
                userMulti = [];
                if (isSP13) {
                    if (typeof SPClientPeoplePicker !== "undefined") {
                        $.each(SPClientPeoplePicker.SPClientPeoplePickerDict[thisField.find("div.sp-peoplepicker-topLevel").attr("id")].GetControlValueAsJSObject(), function (i, o) {
                            if (optionalFilter === 'displayName' || optionalFilter === undefined) {
                                userMulti.push(o.DisplayText);
                            } else if (optionalFilter === 'loginName') {
                                userMulti.push(o.Key);
                            }
                        });
                    } else {
                        userMulti.push("[" + fin + "]: The people picker is not ready!\n\nYou must delay the function call likt this:\nExecuteOrDelayUntilScriptLoaded(function(){/*Your code here*/},'sp.ribbon.js');");
                    }
                } else {
                    thisField.find("div[id='divEntityData']").each(function (i, div) {
                        if (optionalFilter === 'displayName' || optionalFilter === undefined) {
                            userMulti.push($(div).attr("displaytext"));
                        } else if (optionalFilter === 'loginName') {
                            userMulti.push($(div).attr("key"));
                        }
                    });
                }
                if (multiValueJoinBy !== undefined && multiValueJoinBy !== '') {
                    getFieldValueReturnVal = userMulti.join(multiValueJoinBy);
                } else {
                    getFieldValueReturnVal = userMulti;
                }
                break;
            case 'SPFieldLookup':
                if (thisField.find('select option:selected').val() !== '0') {
                    getFieldValueReturnVal = thisField.find('select option:selected').text();
                } else {
                    getFieldValueReturnVal = '';
                }
                break;
            case 'SPFieldLookup_Input':
                getFieldValueReturnVal = thisField.find('input').val();
                break;
            case 'SPFieldLookupMulti':
                lookupMulti = [];
                thisField.find("select:last option").each(function (i, opt) {
                    opt = $(opt);
                    lookupMulti.push(opt.text());
                });
                if (multiValueJoinBy !== undefined && multiValueJoinBy !== '') {
                    getFieldValueReturnVal = lookupMulti.join(multiValueJoinBy);
                } else {
                    getFieldValueReturnVal = lookupMulti;
                }
                break;
            case 'SPFieldBoolean':
                if (parseInt($.fn.jquery.split(".")[1]) >= 6) {
                    getFieldValueReturnVal = (thisField.find('input').prop('checked') === true) ? true : false;
                } else {
                    getFieldValueReturnVal = (thisField.find('input').attr('checked') === true) ? true : false;
                }
                break;
            case 'SPFieldURL':
                link = thisField.find('input:first').val();
                descr = thisField.find('input:last').val();
                getFieldValueReturnVal = "<a href='" + link + "'>" + descr + "</a>";
                break;
            case 'SPFieldDateTime':
                date = thisField.find('input:first').val();
                hour = '';
                minutes = '';
                AMPM = '';
                if (date !== '') {
                    hourRaw = thisField.find('select:first option:selected').val()
                    hour = (hourRaw == null) ? '' : " " + hourRaw.match(/^[\d]+/) + ":";
                    AMPM = (hourRaw == null) ? '' : " " + hourRaw.match(/AM|PM/);
                    minutesRaw = thisField.find('select:last option:selected').val();
                    minutes = (minutesRaw == null) ? '' : minutesRaw;
                }
                getFieldValueReturnVal = date + hour + minutes + AMPM;
                break;
            case 'SPFieldNote':
                getFieldValueReturnVal = thisField.find('textarea:first').val();
                break;
            case 'SPFieldNote_HTML':
                if (browseris.ie5up && browseris.win32 && !IsAccessibilityFeatureEnabled()) {
                    if (thisField.find("iframe[class^='ms-rtelong']").contents().find('body').html() != '') {
                        getFieldValueReturnVal = thisField.find("iframe[class^='ms-rtelong']").contents().find('body').html();
                    } else {
                        getFieldValueReturnVal = thisField.find('textarea:first').val();
                    }
                } else {
                    getFieldValueReturnVal = thisField.find('textarea:first').val();
                }
                break;
            case 'SPFieldNote_EHTML':
                getFieldValueReturnVal = thisField.find("div[id$='TextField_inplacerte']").html();
                break;
            case 'customHeading':
                getFieldValueReturnVal = '';
                break;
            case 'SPFieldTaxonomyFieldType':
            case 'SPFieldTaxonomyFieldTypeMulti':
                getFieldValueReturnVal = [];
                var t = new Microsoft.SharePoint.Taxonomy.ControlObject(thisField.find("div.ms-taxonomy")[0]);
                $(t.getRawText().split(";")).each(function (i, v) {
                    getFieldValueReturnVal.push(v.split("|")[0]);
                });
                getFieldValueReturnVal = getFieldValueReturnVal.join(multiValueJoinBy !== undefined ? multiValueJoinBy : ", ");
                break;
            default:
                getFieldValueReturnVal = "Unknown fieldType: " + fieldType;
        }
        return getFieldValueReturnVal;
    }
}

function init_fields_v2() {
    var res = {};
    $("td.ms-formbody").each(function () {
        var myMatch = $(this).html().match(/FieldName="(.+)"\s+FieldInternalName="(.+)"\s+FieldType="(.+)"\s+/);
        if (myMatch != null) {
            // Display name
            var disp = myMatch[1];
            // FieldInternalName
            var fin = myMatch[2];
            // FieldType
            var type = myMatch[3];
            if (type == 'SPFieldNote') {
                if ($(this).find('script').length > 0) {
                    type = type + "_HTML";
                } else if ($(this).find("div[id$='TextField_inplacerte']").length > 0) {
                    type = type + "_EHTML";
                }
            }
            if (type === "SPFieldLookup") {
                if ($(this).find("input").length > 0) {
                    type = type + "_Input";
                }
            }
            // Build object
            res[fin] = this.parentNode;
            $(res[fin]).attr('FieldDispName', disp);
            $(res[fin]).attr('FieldType', type);
        }
    });
    return res;
}

/* spjs_addItem
	Argument type: object
		Object properties: listName, listBaseUrl, data
		If listBaseUrl is undefined, the current baseUrl, represented with the variable "L_Menu_BaseUrl" is used.
		Note: The property "data" is an object with property=FieldInternalName and value=the new value
	Returned data type: object
		Object properties: success, errorText
*/

function spjs_addItem(argObj) {
    var qRes = spjs_UpdateListItem({
        'action': 'new',
        'listName': argObj.listName,
        'listBaseUrl': argObj.listBaseUrl,
        'data': argObj.data
    });
    return qRes;
}

/* spjs_deleteItem
	Argument type: object
		Object properties: listName, listBaseUrl, id, docFullUrl
		If listBaseUrl is undefined, the current baseUrl, represented with the variable "L_Menu_BaseUrl" is used.
		Note: "docFullUrl" is used whan deleting documents only
			  If used on other than documents, the "id" property can consist of multiple id's separated by comma
			The property id can consist of an array of multiple id's
	Returned data type: object
		Object properties: success, errorText
*/

function spjs_deleteItem(argObj) {
    var qRes = spjs_UpdateListItem({
        'action': 'delete',
        'listName': argObj.listName,
        'listBaseUrl': argObj.listBaseUrl,
        'id': argObj.id,
        'docFullUrl': argObj.docFullUrl
    });
    return qRes;
}

/* spjs_updateItem
	Argument type: object
		Object properties: listName, listBaseUrl, id, data
		If listBaseUrl is undefined, the current baseUrl, represented with the variable "L_Menu_BaseUrl" is used.
		Note: The property "id" can consist of an array of multiple id's
			  The property "data" is an object with property=FieldInternalName and value=the new value
	Returned data type: object
		Object properties: success, errorText
*/

function spjs_updateItem(argObj) {
    var qRes = spjs_UpdateListItem({
        'action': 'update',
        'listName': argObj.listName,
        'listBaseUrl': argObj.listBaseUrl,
        'id': argObj.id,
        'data': argObj.data
    });
    return qRes;
}

function spjs_UpdateListItem(argObj) {
    var content, result, listURL;
    if (argObj.listBaseUrl === undefined) {
        argObj.listBaseUrl = L_Menu_BaseUrl;
    }
    if (argObj.id !== undefined && typeof (argObj.id) !== 'object') {
        argObj.id = argObj.id.toString().split(',');
    }
    content = [];
    content.push("<UpdateListItems xmlns='http://schemas.microsoft.com/sharepoint/soap/'>");
    content.push("<listName>" + argObj.listName + "</listName>");
    content.push("<updates>");
    switch (argObj.action) {
        case 'new':
            if (argObj.folderName != undefined) { // Folder
                content.push("<Batch OnError='Continue' ");
                if (argObj.rootFolder !== undefined) {
                    content.push("RootFolder='" + argObj.rootFolder + "'");
                }
                content.push(" PreCalc='TRUE'>");
                content.push("<Method ID='1' Cmd='New'>");
                content.push("<Field Name='ID'>New</Field>");
                content.push("<Field Name='FSObjType'>1</Field>");
                content.push("<Field Name='BaseName'>" + argObj.folderName + "</Field>");
                content.push("<Field Name='Title'>" + argObj.folderName + "</Field>");
                content.push("</Method>");
            } else { // List
                content.push("<Batch OnError='Continue'>");
                content.push("<Method ID='1' Cmd='New'>");
                content.push("<Field Name='ID'>0</Field>");
                $.each(argObj.data, function (fin, val) {
                    content.push("<Field Name='" + fin + "'><![CDATA[" + val + "]]></Field>");
                });
                content.push("</Method>");
            }
            break;
        case 'update':
            content.push("<Batch OnError='Continue'>");
            $.each(argObj.id, function (i, id) {
                content.push("<Method ID='" + (i + 1) + "' Cmd='Update'>");
                content.push("<Field Name='ID'>" + id + "</Field>");
                $.each(argObj.data, function (fin, val) {
                    content.push("<Field Name='" + fin + "'><![CDATA[" + val + "]]></Field>");
                });
                content.push("</Method>");
            });
            break;
        case 'delete':
            // List items			
            if (argObj.docFullUrl === undefined) {
                content.push("<Batch OnError='Continue'>");
                $.each(argObj.id, function (i, id) {
                    content.push("<Method ID='" + (i + 1) + "' Cmd='Delete'>");
                    content.push("<Field Name='ID'>" + id + "</Field>");
                    content.push("</Method>");
                });
                // Document
            } else if (argObj.id.length == 1 && argObj.docFullUrl !== undefined) {
                content.push("<Batch OnError='Continue' PreCalc='TRUE'>");
                content.push("<Method ID='1' Cmd='Delete'>");
                content.push("<Field Name='ID'>" + argObj.id[0] + "</Field>");
                content.push("<Field Name='FileRef'>" + argObj.docFullUrl + "</Field>");
                content.push("</Method>");
            } else {
                alert("You cannot delete more than one document at a time!");
            }
            break;
    }
    content.push("</Batch>");
    content.push("</updates>");
    content.push("</UpdateListItems>");
    content = content.join('');
    result = {
        success: false,
        errorText: null,
        errorCode: null
    };
    spjs_wrapSoapRequest(argObj.listBaseUrl + '/_vti_bin/lists.asmx', 'http://schemas.microsoft.com/sharepoint/soap/UpdateListItems', content, function (data) {
        if ($(data).find('ErrorText').length > 0) {
            result.errorText = $(data).find('ErrorText').text();
            result.errorCode = $(data).find('ErrorCode').text();
            // Duplicate folder names			
            if (result.errorCode == '0x8107090d' && argObj.folderName !== undefined) {
                result.errorText = "A folder named \"" + argObj.folderName + "\" already exist";
            }
        } else {
            result.success = true;
            if (argObj.action !== 'delete') {
                result.id = $(data).filterNode('z:row').attr('ows_ID');
                listURL = $(data).filterNode('z:row').attr('ows_EncodedAbsUrl');
                result.listURL = listURL.substring(0, listURL.lastIndexOf('/'));
            }
        }
    });
    return result;
}