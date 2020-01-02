/*
 * This file launches the application by asking Ext JS to create
 * and launch() the Application class.
 */
Ext.application({
    extend: 'acsweb.Application',

    name: 'acsweb',

    requires: [
        // This will automatically load all classes in the jz namespace
        // so that application classes do not need to require each other.
        'acsweb.*'
    ],

    // The name of the initial view to create.
    mainView: 'acsweb.view.main.Main'
});
