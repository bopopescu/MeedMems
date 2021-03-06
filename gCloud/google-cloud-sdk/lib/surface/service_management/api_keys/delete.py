# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Implementation of the service-management api-keys delete command."""

from googlecloudsdk.api_lib.service_management import base_classes
from googlecloudsdk.api_lib.service_management import services_util

from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import resource_printer
from googlecloudsdk.third_party.apitools.base.py import exceptions as apitools_exceptions


class Delete(base.Command, base_classes.BaseServiceManagementCommand):
  """Deletes a key."""

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    parser.add_argument('--key',
                        '-k',
                        help='The identifier of the key to be deleted.')

  def Run(self, args):
    """Run 'service-management api-keys delete'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.

    Returns:
      The response from the keys API call.

    Raises:
      HttpException: An http error response was received while executing api
          request.
    """
    # Construct the Delete API Key request object
    request = self.apikeys_messages.ApikeysProjectsApiKeysDeleteRequest(
        projectId=self.project,
        keyId=args.key)

    try:
      return self.apikeys_client.projects_apiKeys.Delete(request)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(services_util.GetError(error))

  def Display(self, args, result):
    """Display prints information about what just happened to stdout.

    Args:
      args: The same as the args in Run.

      result: an Operation object

    Raises:
      TypeError: if result is not of type Operation
    """
    # Note that we expect an empty proto as a result (google.protobuf.Empty).
    # If that's what we receive, we can assume success.
    if not result:
      resource_printer.Print(resources={'key': args.key, 'success': True},
                             print_format=args.format or 'yaml',
                             out=log.out)
