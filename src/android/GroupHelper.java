package com.q.users.cordova.plugin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.q.users.cordova.plugin.RawIdLabelId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupHelper {


    /**
     * Gets all existing rawContactId and label id pairs
     *
     * @param context Context instance for db interactions
     * @return RawIdLabelId object that contains {@link RawIdLabelId#labelId} and {@link RawIdLabelId#rawId}
     */
    public static List<RawIdLabelId> getExistingRawIdLabelIdPairs(Context context) {
        List<RawIdLabelId> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.Data.RAW_CONTACT_ID,
                        ContactsContract.Data.DATA1
                },
                ContactsContract.Data.MIMETYPE + "='" +
                        ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'",
                null,
                null);
        while (cursor.moveToNext()) {
            RawIdLabelId rawIdLabelId = new RawIdLabelId(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1)));
            list.add(rawIdLabelId);
        }
        cursor.close();
        return list;
    }

    /**
     * Gets all existing contactIds for all rawContactId pairs.
     *
     * @param context Context instance for db interactions
     * @return HashMap that contains rawContactId and contact id
     * (key - rawContactId, value - contact id)
     */
    public static HashMap<String, String> getExistingRawIdContactIdPairs(Context context) {
        HashMap<String, String> map = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID
                },
                null,
                null,
                null);
        while (cursor.moveToNext()) {
            map.put(cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)));
        }
        cursor.close();
        return map;
    }

    /**
     * Converts given rawContactId/contactId pairs into only contact id list.
     *
     * @param rawIdContactId HashMap that contains rawContactId(key) and contactId(value)
     * @param rawIds         List of rawContactIds that needed to be converted.
     * @return List converted contactIds
     */
    public static List<Integer> getContactIds(HashMap<String, String> rawIdContactId, List<Integer> rawIds) {
        List<Integer> contactIds = new ArrayList<>();
        for (int i = 0; i < rawIds.size(); i++) {
            if (!contactIds.contains(Integer.valueOf(rawIdContactId.get(String.valueOf(rawIds.get(i)))))) {
                contactIds.add(Integer.valueOf(rawIdContactId.get(String.valueOf(rawIds.get(i)))));
                Log.i("contactId_checker", "rawId: " + rawIds.get(i) + "\ncontactId: " + rawIdContactId.get(String.valueOf(rawIds.get(i))));
            } else {
                Log.i("contactId_checker", "contains: " + rawIdContactId.get(String.valueOf(rawIds.get(i))));
            }
        }
        return rawIds;
    }

    /**
     * Builds a string for selection query, based on selection arguments count.
     *
     * @param count Size of selectionArgs
     * @return string for query selection (example: "IN(?,?...,?)")
     */
    public static String getSuffix(int count) {
        String selectionSuffix = " IN(";
        if (count == 1) {
            //checks if there is 1 argument
            selectionSuffix += "?)";
        } else {
            for (int i = 0; i < count; i++) {
                if (i == 0) {
                    //for first argument
                    selectionSuffix += "?";
                } else if (i != (count - 1)) {
                    //for following arguments
                    selectionSuffix += ",?";
                } else {
                    //for last argument
                    selectionSuffix += ",?)";
                }
            }
        }
        Log.d("suffix_checker", "count: " + count + " suffix: " + selectionSuffix);
        return selectionSuffix;
    }

    /**
     * Gets all rawContactId's for given contactId array.
     *
     * @param context Context instance for db interactions
     * @param contactIds Array of contactIds which rawContactId's are needed
     * @return rawContactId array
     */
    public static String[] getRawContactIds(Context context, String[] contactIds) {
        List<String> rawIdList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + GroupHelper.getSuffix(contactIds.length),
                contactIds, null
        );
        while (cursor.moveToNext()) {
            rawIdList.add(cursor.getString(0));
        }
        cursor.close();
        String[] rawIdArray = new String[rawIdList.size()];
        for (int i = 0; i < rawIdArray.length; i++) {
            rawIdArray[i] = rawIdList.get(i);
            Log.d("raw_id_checker", rawIdArray[i]);
        }
        return rawIdArray;
    }

    /**
     * Gets all account names of for rawContactIds and set them into HashMap.
     *
     * @param context Context instance for db interactions
     * @param rawContactIds rawContactIds which account names wanted to be returned
     * @return HashMap that contains rawContactId and its account name
     * (key - raw contact id, value - account name)
     */
    public static HashMap<String, String> getRawContactIdAccountNamePair(Context context, String[] rawContactIds) {
        HashMap<String, String> map = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.ACCOUNT_NAME
                },
                ContactsContract.RawContacts._ID + GroupHelper.getSuffix(rawContactIds.length),
                rawContactIds,
                null);
        while (cursor.moveToNext()) {
            map.put(cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)));
        }
        cursor.close();
        return map;
    }

    /**
     * Gets all label ids for sourceId and binds them to their account names.
     *
     * @param context Context instance for db interactions
     * @param sourceId sourceId which labelIds wanted to be returned
     * @return HashMap that contains rawContactId and its account name
     * (key - account name, value - label id)
     */
    public static HashMap<String, String> getAccountNameLabelIdPair(Context context, String sourceId) {
        HashMap<String, String> map = new HashMap<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.SOURCE_ID,
                        ContactsContract.Groups.ACCOUNT_NAME
                },
                ContactsContract.Groups.SOURCE_ID + "='" + sourceId + "'",
                null,
                null);
        while (cursor.moveToNext()) {
            map.put(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.ACCOUNT_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID)));
        }
        cursor.close();
        return map;
    }

    /**
     * Gets all existing labels for given rawContactIds.
     *
     * @param context Context instance for db interactions
     * @param rawContactIds Array of rawContactIds which labels wanted to be returned
     * @return HashMap that contains rawContactId and label id
     * (key - rawContactId, value - label id)
     */
    public static List<RawIdLabelId> getExistingRawIdLabelIdPairs(Context context, String[] rawContactIds) {
        List<RawIdLabelId> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.Data.RAW_CONTACT_ID,
                        ContactsContract.Data.DATA1
                },
                ContactsContract.Data.MIMETYPE + "='" +
                        ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE +
                        "' AND " + ContactsContract.Data.RAW_CONTACT_ID + GroupHelper.getSuffix(rawContactIds.length),
                rawContactIds,
                null);
        while (cursor.moveToNext()) {
            list.add(new RawIdLabelId(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1))));
        }
        cursor.close();
        return list;
    }

    /**
     * @param context Context instance for getting account manager
     *
     * Forces the system to sync all accounts.(if you dont sync some deleted data can be shown to user
     * as before till system syncs automatically).
     */
    public static void requestSyncNow(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                AccountManager accountManager = AccountManager.get(context);
                Account[] accounts = accountManager.getAccounts();
                boolean isMasterSyncOn = ContentResolver.getMasterSyncAutomatically();
                for (Account account : accounts) {

                    Log.d("sync_checker", "account=" + account);
                    int isSyncable = ContentResolver.getIsSyncable(account,
                            ContactsContract.AUTHORITY);
                    boolean isSyncOn = ContentResolver.getSyncAutomatically(account,
                            ContactsContract.AUTHORITY);
                    Log.d("sync_checker", "Syncable=" + isSyncable + " SyncOn=" + isSyncOn);
                    if (isSyncable > 0 /* && isSyncOn */) {
                        Log.d("sync_checker", "request Sync");
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS, true);
                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                        ContentResolver.requestSync(account, ContactsContract.AUTHORITY, bundle);
                    }
                }
            }
        }, "SyncLauncher").start();

    }

    /**
     * Gets all rawContacts' ids which have labels with given sourceId.
     *
     * @param context Context instance for db interactions
     * @param sourceId SourceId of labels which rawContacts' ids must be returned
     * @return List of rawContactIds
     */
    public static List<Integer> getRawIdsBySourceId(Context context, String sourceId) {
        List<Integer> rawIds = new ArrayList<>();
        String[] labelIds = getLabelIdsForSourceId(context, sourceId);
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.RAW_CONTACT_ID,
                        ContactsContract.Data.DATA1
                },
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "' AND " +
                        ContactsContract.Data.DATA1 + getSuffix(labelIds.length),
                labelIds,
                null);
        while (cursor.moveToNext()) {
            rawIds.add(Integer.valueOf(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID))));
        }
        cursor.close();
        return rawIds;
    }

    /**
     * Converts given sourceId to Array of labelIds with that sourceId.
     *
     * @param context Context instance for db interactions
     * @param sourceId SourceId which must be converted into labelId array
     * @return Array of labelIds converted from given sourceId
     */
    public static String[] getLabelIdsForSourceId(Context context, String sourceId) {
        List<String> labelIdList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.SOURCE_ID
                },
                ContactsContract.Groups.SOURCE_ID + "='" + sourceId + "'",
                null,
                null);
        while (cursor.moveToNext()) {
            labelIdList.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID)));
        }
        String[] labelIdArray = new String[labelIdList.size()];
        for (int i = 0; i < labelIdArray.length; i++) {
            labelIdArray[i] = labelIdList.get(i);
        }
        return labelIdArray;
    }

    /**
     * Gets all system related label ids.
     *
     * @param context Context instance for db interactions
     * @return List of system related labelIds
     */
    public static List<String> getSystemIds(Context context) {
        List<String> systemIds = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID
                },
                ContactsContract.Groups.TITLE + getSuffix(2),   //System related labels count
                new String[]{"My Contacts", "Starred in Android"},   //System related labels' titles
                null
        );
        while (cursor.moveToNext()) {
            systemIds.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID)));
        }
        cursor.close();
        return systemIds;
    }

    /**
     * Gets given rawContactId's account name.
     *
     * @param rawContactId rawContactId which account name wanted to be returned
     * @return account name
     */
    public static String getRawContactIdAccountName(Context context, String rawContactId) {
        String accountName = null;
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.ACCOUNT_NAME
                },
                ContactsContract.RawContacts._ID + "='" + rawContactId + "'",
                null,
                null);
        while (cursor.moveToNext()) {
            accountName = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
        }
        cursor.close();
        return accountName;
    }

    /**
     * Gets all sourceIds for union selection.
     * (See {@link GroupAccessor#getLabelsByContactIds(List, boolean)} method description)
     *
     * @param context Context instance for db interactions
     * @param rawIds Raw Contact Ids of contacts which label sourceIds wanted to be returned
     * @return List of sourceIds
     */
    public static String[] getUnionSourceIds(Context context, String[] rawIds) {
        String[] labelIds = getUniqueLabelIdsForRawIds(context, rawIds);
        String[] sourceIds = getSourceIdsForLabelIds(context, labelIds);
        return sourceIds;
    }

    /**
     * Gets all unique label ids that belongs to given rawIds.
     *
     * @param context Context instance for db interactions
     * @param rawIds Raw Contact Ids which unique labels wanted to be returned
     * @return Array of unique label ids
     */
    public static String[] getUniqueLabelIdsForRawIds(Context context, String[] rawIds) {
        List<String> labelIdList = new ArrayList<>();
        List<String> systemIds = getSystemIds(context);
        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.DATA1
                },
                ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                        + "' AND " + ContactsContract.Data.RAW_CONTACT_ID + getSuffix(rawIds.length),
                rawIds,
                null);
        while (cursor.moveToNext()) {
            String labelId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
            if (labelId != null && !labelIdList.contains(labelId)&&!systemIds.contains(labelId)) {
                labelIdList.add(labelId);
            }
        }
        cursor.close();
        String[] labelIdArray = new String[labelIdList.size()];
        for (int i = 0; i < labelIdArray.length; i++) {
            labelIdArray[i] = labelIdList.get(i);
        }
        return labelIdArray;
    }

    /**
     * Gets all source Ids of given labelId list.
     *
     * @param context Context instance for db interactions
     * @param labelIds Label Ids which source ids wanted to be returned
     * @return Array of source ids
     */
    public static String[] getSourceIdsForLabelIds(Context context, String[] labelIds){
        List<String> sourceIdList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups.SOURCE_ID
                },
                ContactsContract.Groups._ID+ getSuffix(labelIds.length),
                labelIds,
                null);
        while (cursor.moveToNext()){
            String sourceId = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID));
            if(sourceId!=null&&!sourceIdList.contains(sourceId)){
                sourceIdList.add(sourceId);
            }
        }
        cursor.close();
        String[] sourceIdArray = new String[sourceIdList.size()];
        for (int i = 0; i < sourceIdArray.length; i++) {
            sourceIdArray[i] = sourceIdList.get(i);
        }
        return sourceIdArray;
    }

    /**
     * Gets all sourceIds for not union selection.
     * (See {@link GroupAccessor#getLabelsByContactIds(List, boolean)} method description)
     *
     * @param context Context instance for db interactions
     * @param rawIds Raw Contact Ids of contacts which label sourceIds wanted to be returned
     * @return Array of sourceIds which labels have all given rawContactIds
     */
    public static String[] getNotUnionSourceIds(Context context, String[] rawIds) {

        List<String> sourceIdList = new ArrayList<>();
        HashMap<String, String> labelIdTitlePair = new HashMap<>();
        List<String> systemLabelIds = getSystemIds(context);
        List<String> accNameList = getAccountNamesForRawIds(context, rawIds);
        String[] accNameArray = new String[accNameList.size()];
        for (int i = 0; i < accNameList.size(); i++) {
            accNameArray[i] = accNameList.get(i);
        }
        List<String> allTitles = new ArrayList<>();
        List<RawIdTitles> rawIdTitlesList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.ACCOUNT_NAME,
                        ContactsContract.Groups.TITLE
                },
                null,
                null,
                null);
        while (cursor.moveToNext()) {
            labelIdTitlePair.put(cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID)),
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE)));

        }
        cursor.close();
        for (int i = 0; i < rawIds.length; i++) {
            RawIdTitles rawTitlePair = new RawIdTitles(rawIds[i]);
            List<String> titles = new ArrayList<>();
            Cursor rawCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{
                            ContactsContract.Data.RAW_CONTACT_ID,
                            ContactsContract.Data.DATA1
                    },
                    ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                            + "' AND " + ContactsContract.Data.RAW_CONTACT_ID + "='" + rawIds[i] + "'",
                    null,
                    null);
            while (rawCursor.moveToNext()) {
                String title = labelIdTitlePair.get(rawCursor.getString(rawCursor.getColumnIndex(ContactsContract.Data.DATA1)));
                if (title != null) {
                    titles.add(title);
                }
                if (!allTitles.contains(title)) {
                    allTitles.add(title);
                }
            }
            rawCursor.close();
            rawTitlePair.titles = titles;
            rawIdTitlesList.add(rawTitlePair);
        }
        for (int i = 0; i < allTitles.size(); i++) {
            boolean contains = true;
            for (int j = 0; j < rawIdTitlesList.size(); j++) {
                if (!rawIdTitlesList.get(j).titles.contains(allTitles.get(i))) {
                    contains = false;
                    break;
                }
            }

            if (contains) {
                Cursor labelCursor = context.getContentResolver().query(ContactsContract.Groups.CONTENT_URI,
                        new String[]{
                                ContactsContract.Groups.TITLE,
                                ContactsContract.Groups.ACCOUNT_NAME,
                                ContactsContract.Groups._ID,
                                ContactsContract.Groups.SOURCE_ID
                        },
                        ContactsContract.Groups.TITLE + "='" + allTitles.get(i)
                                + "' AND " + ContactsContract.Groups.ACCOUNT_NAME + getSuffix(accNameArray.length),
                        accNameArray,
                        null);
                while (labelCursor.moveToNext()) {
                    if (!systemLabelIds.contains(labelCursor.getString(labelCursor.getColumnIndex(ContactsContract.Groups._ID)))) {
                        sourceIdList.add(labelCursor.getString(labelCursor.getColumnIndex(ContactsContract.Groups.SOURCE_ID)));
                    }
                }
                labelCursor.close();
            }
        }
        String[] sourceIdArray = new String[sourceIdList.size()];
        for (int i = 0; i < sourceIdArray.length; i++) {
            sourceIdArray[i] = sourceIdList.get(i);
        }
        return sourceIdArray;
    }

    /**
     * Gets all unique Account Names for given rawContactIds.
     *
     * @param context Context instance for db interactions
     * @param rawIds Raw Contact Ids which account names wanted to be returned
     * @return List of unique account names
     */
    public static List<String> getAccountNamesForRawIds(Context context, String[] rawIds) {
        List<String> accNames = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.ACCOUNT_NAME
                },
                ContactsContract.RawContacts._ID + getSuffix(rawIds.length),
                rawIds,
                null);
        while (cursor.moveToNext()) {
            String accName = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
            if (!accNames.contains(accName)) {
                accNames.add(accName);
            }
        }
        cursor.close();
        return accNames;
    }
}
